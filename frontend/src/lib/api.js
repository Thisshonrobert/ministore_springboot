//member 3
const API_BASE = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '')
const UNSAFE_METHODS = new Set(['POST', 'PUT', 'PATCH', 'DELETE'])

let csrfToken = null

export class ApiError extends Error {
  constructor(message, status, fieldErrors = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

function readCookie(name) {
  if (typeof document === 'undefined') {
    return null
  }

  const prefix = `${name}=`
  for (const part of document.cookie.split(';')) {
    const cookie = part.trim()
    if (cookie.startsWith(prefix)) {
      return decodeURIComponent(cookie.slice(prefix.length))
    }
  }
  return null
}

async function parseJson(response) {
  const text = await response.text()
  if (!text) {
    return null
  }

  try {
    return JSON.parse(text)
  } catch {
    return null
  }
}

export async function fetchCsrfToken() {
  const response = await fetch(`${API_BASE}/auth/csrf`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
  })

  if (!response.ok) {
    throw new ApiError('Could not start a secure session. Refresh the page and try again.', response.status)
  }

  const body = await response.json()
  csrfToken = body.token
  return csrfToken
}

async function ensureCsrfToken() {
  const cookieToken = readCookie('XSRF-TOKEN')
  if (cookieToken) {
    csrfToken = cookieToken
    return csrfToken
  }

  if (!csrfToken) {
    return fetchCsrfToken()
  }

  return csrfToken
}

export function resetCsrfToken() {
  csrfToken = null
}

function fallbackMessage(status) {
  if (status === 401) return 'Please sign in to continue.'
  if (status === 403) return 'That request was blocked for security reasons.'
  if (status === 404) return 'We could not find that resource.'
  if (status === 409) return 'That change conflicts with an existing record.'
  if (status >= 500) return 'The store had a problem. Please try again.'
  return 'Something went wrong. Please try again.'
}

async function send(path, options, retried) {
  const method = (options.method || 'GET').toUpperCase()
  const headers = { Accept: 'application/json', ...(options.headers || {}) }
  let body

  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
    body = JSON.stringify(options.body)
  }

  if (UNSAFE_METHODS.has(method)) {
    headers['X-XSRF-TOKEN'] = await ensureCsrfToken()
  }

  let response
  try {
    response = await fetch(`${API_BASE}${path}`, {
      method,
      credentials: 'include',
      headers,
      body,
      signal: options.signal,
    })
  } catch (error) {
    if (error.name === 'AbortError') {
      throw error
    }
    throw new ApiError('The store API is unreachable. Is the Spring Boot app running?', 0)
  }

  // Login and registration rotate the CSRF token; the response header keeps a
  // cross-origin client in sync even when it cannot read the cookie directly.
  const rotatedToken = response.headers.get('X-CSRF-TOKEN')
  if (rotatedToken) {
    csrfToken = rotatedToken
  }

  if (response.status === 204) {
    return null
  }

  const data = await parseJson(response)

  if (!response.ok) {
    if (response.status === 403 && UNSAFE_METHODS.has(method) && !retried) {
      // The session or token may have expired. Start a fresh CSRF session once.
      await fetchCsrfToken()
      return send(path, options, true)
    }

    throw new ApiError(
      data?.message || fallbackMessage(response.status),
      response.status,
      data?.fieldErrors || {},
    )
  }

  return data
}

export function api(path, options = {}) {
  return send(path, options, false)
}
