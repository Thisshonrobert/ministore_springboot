import { api } from '@/lib/api'

/**
 * Every customer gets a cart on registration. Older accounts may not have one,
 * so create it on demand and tolerate a simultaneous create.
 */
export async function getOrCreateCart() {
  try {
    return await api('/me/cart')
  } catch (error) {
    if (error.status !== 404) {
      throw error
    }

    try {
      return await api('/me/cart', { method: 'POST' })
    } catch (createError) {
      if (createError.status === 409) {
        return api('/me/cart')
      }
      throw createError
    }
  }
}

export function cartItemCount(cart) {
  return (cart?.items || []).reduce((total, item) => total + item.quantity, 0)
}
