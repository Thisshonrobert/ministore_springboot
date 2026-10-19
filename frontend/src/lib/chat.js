//member 4
import { api } from '@/lib/api'

/**
 * The shopping assistant answers one question at a time. The reply always
 * comes from the backend, which looks the products up in the database first.
 */
export function askShopAssistant(message) {
  return api('/chat', { method: 'POST', body: { message } })
}
