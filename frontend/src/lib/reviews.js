//member 4
import { api } from '@/lib/api'

/**
 * Review endpoints. Writing a review goes through /me, so the backend takes the
 * author from the session: the page never sends a customer ID.
 */
export function fetchProductReviews(productId) {
  return api(`/products/${productId}/reviews`)
}

export function summarizeProductReviews(productId) {
  return api(`/products/${productId}/reviews/summary`, { method: 'POST' })
}

export function createProductReview({ productId, rating, comment }) {
  return api('/me/reviews', { method: 'POST', body: { productId, rating, comment } })
}
