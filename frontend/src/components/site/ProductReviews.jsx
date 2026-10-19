//member 4
import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { toast } from 'sonner'
import { RefreshCcw, Sparkles, Star } from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { StarRating } from '@/components/site/StarRating'
import { AiText } from '@/components/site/AiText'
import { useAuth } from '@/context/AuthContext'
import { formatDate, pluralize } from '@/lib/format'
import { createProductReview, fetchProductReviews, summarizeProductReviews } from '@/lib/reviews'
import { cn } from '@/lib/utils'

const MAX_COMMENT = 1000

function ReviewSkeleton() {
  return (
    <div className="space-y-3" aria-hidden="true">
      <Skeleton className="h-5 w-40" />
      <Skeleton className="h-4 w-full" />
      <Skeleton className="h-4 w-3/4" />
    </div>
  )
}

/**
 * The review section of a product page: the real reviews, the AI summary of
 * them, and the form a signed-in customer uses to add their own review.
 */
export function ProductReviews({ productId }) {
  const { user } = useAuth()
  const location = useLocation()

  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [summarizing, setSummarizing] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setData(await fetchProductReviews(productId))
    } catch (requestError) {
      setError(requestError)
    } finally {
      setLoading(false)
    }
  }, [productId])

  useEffect(() => {
    load()
  }, [load])

  async function submitReview(event) {
    event.preventDefault()
    if (!user || submitting || !comment.trim()) {
      return
    }

    setSubmitting(true)
    try {
      await createProductReview({ productId, rating, comment })
      setComment('')
      setRating(5)
      toast.success('Thank you, your review is on the page.')
      await load()
    } catch (requestError) {
      toast.error(requestError.message || 'Could not save that review.')
    } finally {
      setSubmitting(false)
    }
  }

  async function summarize() {
    setSummarizing(true)
    try {
      setData(await summarizeProductReviews(productId))
      toast.success('The summary is ready.')
    } catch (requestError) {
      toast.error(requestError.message || 'Could not summarise the reviews.')
    } finally {
      setSummarizing(false)
    }
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <Card className="rounded-2xl">
          <CardContent className="pt-6">
            <ReviewSkeleton />
          </CardContent>
        </Card>
      </div>
    )
  }

  if (error) {
    return (
      <Alert variant="destructive">
        <RefreshCcw className="size-4" aria-hidden="true" />
        <AlertTitle>The reviews did not load</AlertTitle>
        <AlertDescription>
          <p>{error.message}</p>
          <Button variant="outline" size="sm" className="mt-3" onClick={load}>
            <RefreshCcw className="size-4" aria-hidden="true" />
            Try again
          </Button>
        </AlertDescription>
      </Alert>
    )
  }

  const reviews = data?.reviews || []
  const summary = data?.summary || null
  const summaryMessage =
    data?.summaryMessage ||
    (reviews.length === 0
      ? 'There are no reviews yet, so there is nothing to summarise.'
      : 'No summary has been written yet. You can ask the assistant to read the reviews below.')

  return (
    <div className="space-y-6">
      <Card className="rounded-2xl border-primary/15 bg-secondary/45">
        <CardHeader>
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <span className="grid size-10 place-items-center rounded-full bg-card">
                <Sparkles className="size-5 text-accent" aria-hidden="true" />
              </span>
              <div>
                <CardTitle className="text-xl">What reviewers say</CardTitle>
                <CardDescription>
                  Written by AI from the {pluralize(reviews.length, 'review')} on this product.
                </CardDescription>
              </div>
            </div>
            <Badge variant="accent">AI summary</Badge>
          </div>
        </CardHeader>

        <CardContent className="space-y-4">
          {summary ? (
            <>
              <AiText text={summary.content} className="text-sm leading-relaxed" />
              <p className="text-xs text-muted-foreground">
                Generated {formatDate(summary.generatedAt)}. A new review replaces this summary.
              </p>
            </>
          ) : (
            <>
              <p className="text-sm leading-relaxed text-muted-foreground">{summaryMessage}</p>
              {reviews.length > 0 && (
                <Button variant="outline" onClick={summarize} disabled={summarizing}>
                  {summarizing ? (
                    <RefreshCcw className="size-4 animate-spin" aria-hidden="true" />
                  ) : (
                    <Sparkles className="size-4" aria-hidden="true" />
                  )}
                  {summarizing ? 'Reading the reviews…' : 'Summarise the reviews'}
                </Button>
              )}
            </>
          )}
        </CardContent>
      </Card>

      {user ? (
        <Card className="rounded-2xl">
          <CardHeader>
            <CardTitle className="text-xl">Write a review</CardTitle>
            <CardDescription>
              Your review is saved against your account, so the page never sends a customer number.
            </CardDescription>
          </CardHeader>

          <CardContent>
            <form className="space-y-5" onSubmit={submitReview}>
              <div>
                <p className="text-xs font-semibold tracking-[0.14em] text-muted-foreground uppercase">
                  Rating
                </p>
                <div className="mt-2 flex items-center gap-1">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <button
                      key={star}
                      type="button"
                      onClick={() => setRating(star)}
                      aria-pressed={rating === star}
                      aria-label={`${star} star${star === 1 ? '' : 's'}`}
                      className="rounded-md p-1 transition-transform hover:-translate-y-0.5"
                    >
                      <Star
                        className={cn(
                          'size-6',
                          star <= rating ? 'fill-accent text-accent' : 'text-border',
                        )}
                        aria-hidden="true"
                      />
                    </button>
                  ))}
                  <span className="ml-2 text-sm text-muted-foreground">{rating} of 5</span>
                </div>
              </div>

              <div className="space-y-2">
                <label
                  htmlFor={`review-comment-${productId}`}
                  className="text-xs font-semibold tracking-[0.14em] text-muted-foreground uppercase"
                >
                  Your review
                </label>
                <textarea
                  id={`review-comment-${productId}`}
                  value={comment}
                  onChange={(event) => setComment(event.target.value)}
                  rows={4}
                  maxLength={MAX_COMMENT}
                  required
                  placeholder="What did you like, and what could be better?"
                  className="flex w-full min-w-0 rounded-md border border-input bg-card/70 px-3 py-2 text-base shadow-xs outline-none transition-[color,box-shadow,border-color] placeholder:text-muted-foreground/80 focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/30 md:text-sm"
                />
                <p className="text-xs text-muted-foreground">
                  {comment.length}/{MAX_COMMENT} characters
                </p>
              </div>

              <Button type="submit" disabled={submitting || !comment.trim()}>
                {submitting ? 'Saving…' : 'Publish my review'}
              </Button>
            </form>
          </CardContent>
        </Card>
      ) : (
        <Card className="rounded-2xl bg-secondary/45">
          <CardContent className="flex flex-col gap-4 pt-6 sm:flex-row sm:items-center sm:justify-between">
            <p className="max-w-xl text-sm leading-relaxed text-muted-foreground">
              Sign in to add your own rating and review. Reviews belong to the customer in the
              session, not to anything typed into the page.
            </p>
            <Button asChild>
              <Link to="/login" state={{ from: location.pathname }}>
                Sign in to review
              </Link>
            </Button>
          </CardContent>
        </Card>
      )}

      <Card className="rounded-2xl">
        <CardHeader>
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <CardTitle className="text-xl">
                {reviews.length === 0 ? 'No reviews yet' : pluralize(reviews.length, 'review')}
              </CardTitle>
              <CardDescription>
                {data?.averageRating === null || data?.averageRating === undefined
                  ? 'This product has not been rated yet.'
                  : `Average rating ${data.averageRating} out of 5.`}
              </CardDescription>
            </div>
            {data?.averageRating !== null && data?.averageRating !== undefined && (
              <StarRating value={data.averageRating} />
            )}
          </div>
        </CardHeader>

        <CardContent>
          {reviews.length === 0 ? (
            <p className="text-sm leading-relaxed text-muted-foreground">
              Be the first to say what you think about this product.
            </p>
          ) : (
            <ul className="space-y-5">
              {reviews.map((review) => (
                <li key={review.id} className="space-y-1.5">
                  <div className="flex flex-wrap items-center gap-x-3 gap-y-1">
                    <span className="font-display text-lg font-semibold">
                      {review.authorName || 'Customer'}
                    </span>
                    <StarRating value={review.rating} />
                    <span className="text-xs text-muted-foreground">
                      {formatDate(review.createdAt)}
                    </span>
                  </div>
                  <p className="text-sm leading-relaxed text-muted-foreground">{review.comment}</p>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
