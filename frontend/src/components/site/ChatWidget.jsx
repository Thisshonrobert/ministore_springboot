//member 4
import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { toast } from 'sonner'
import { Loader2, MessageCircle, Send, Sparkles, X } from 'lucide-react'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { AiText } from '@/components/site/AiText'
import { formatPrice } from '@/lib/format'
import { askShopAssistant } from '@/lib/chat'

const MAX_QUESTION = 1000

/**
 * A small floating panel that asks the backend assistant about the catalogue.
 * Every message is sent on its own: the backend looks products up in the
 * database, so the widget only renders what comes back.
 */
export function ChatWidget() {
  const [open, setOpen] = useState(false)
  const [question, setQuestion] = useState('')
  const [asking, setAsking] = useState(false)
  const [error, setError] = useState(null)
  const [messages, setMessages] = useState([])

  const logRef = useRef(null)

  useEffect(() => {
    if (logRef.current) {
      logRef.current.scrollTop = logRef.current.scrollHeight
    }
  }, [messages, open])

  async function ask(event) {
    event.preventDefault()
    const trimmed = question.trim()
    if (!trimmed || asking) {
      return
    }

    setMessages((current) => [...current, { role: 'you', text: trimmed }])
    setQuestion('')
    setAsking(true)
    setError(null)

    try {
      const answer = await askShopAssistant(trimmed)
      setMessages((current) => [
        ...current,
        { role: 'shop', text: answer.reply, products: answer.products || [] },
      ])
    } catch (requestError) {
      setError(requestError)
      toast.error(requestError.message || 'The assistant did not answer.')
    } finally {
      setAsking(false)
    }
  }

  return (
    <div className="fixed right-4 bottom-4 z-50 flex flex-col items-end gap-3 sm:right-6 sm:bottom-6">
      {open && (
        <Card className="w-[min(22rem,calc(100vw-2rem))] gap-0 overflow-hidden rounded-2xl border-primary/20 shadow-[0_30px_70px_-40px_rgba(31,77,58,0.7)]">
          <CardHeader className="border-b border-border/70 bg-secondary/45">
            <div className="flex items-start justify-between gap-3">
              <div className="flex items-center gap-3">
                <span className="grid size-9 place-items-center rounded-full bg-card">
                  <Sparkles className="size-4 text-accent" aria-hidden="true" />
                </span>
                <div>
                  <CardTitle className="text-lg">Shop assistant</CardTitle>
                  <CardDescription>Answers come from the store database.</CardDescription>
                </div>
              </div>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                onClick={() => setOpen(false)}
                aria-label="Close the shop assistant"
              >
                <X className="size-4" aria-hidden="true" />
              </Button>
            </div>
          </CardHeader>

          <CardContent className="flex flex-col gap-3 p-4">
            <div
              ref={logRef}
              className="max-h-72 min-h-32 space-y-3 overflow-y-auto pr-1"
              aria-live="polite"
            >
              {messages.length === 0 ? (
                <p className="text-sm leading-relaxed text-muted-foreground">
                  Ask about the shelves, a price, or which shelf a product is on. For example:
                  “What notebooks do you have?” The assistant cannot place orders or change your
                  cart.
                </p>
              ) : (
                messages.map((message, index) => (
                  <div
                    key={index}
                    className={
                      message.role === 'you'
                        ? 'ml-auto max-w-[85%] rounded-2xl rounded-br-sm bg-primary px-3 py-2 text-sm text-primary-foreground'
                        : 'max-w-[92%] rounded-2xl rounded-bl-sm bg-secondary/60 px-3 py-2 text-sm leading-relaxed'
                    }
                  >
                    {message.role === 'you' ? (
                      <p className="whitespace-pre-line">{message.text}</p>
                    ) : (
                      <AiText text={message.text} />
                    )}
                    {message.products?.length > 0 && (
                      <ul className="mt-2 space-y-1.5 border-t border-border/60 pt-2">
                        {message.products.map((product) => (
                          <li key={product.id}>
                            <Link
                              to={`/products/${product.id}`}
                              className="flex items-center justify-between gap-3 rounded-lg px-2 py-1 transition-colors hover:bg-card"
                            >
                              <span className="min-w-0 truncate font-medium">{product.name}</span>
                              <span className="shrink-0 font-mono text-xs text-muted-foreground">
                                {formatPrice(product.price)}
                              </span>
                            </Link>
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                ))
              )}

              {asking && (
                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                  <Loader2 className="size-3.5 animate-spin" aria-hidden="true" />
                  Looking through the shelves…
                </div>
              )}
            </div>

            {error && (
              <Alert variant="destructive">
                <AlertTitle>The assistant is not answering</AlertTitle>
                <AlertDescription>{error.message}</AlertDescription>
              </Alert>
            )}

            <form className="space-y-2" onSubmit={ask}>
              <label htmlFor="shop-assistant-question" className="sr-only">
                Your question for the shop assistant
              </label>
              <textarea
                id="shop-assistant-question"
                value={question}
                onChange={(event) => setQuestion(event.target.value)}
                rows={2}
                maxLength={MAX_QUESTION}
                placeholder="Ask about a product or a price…"
                className="flex w-full min-w-0 resize-none rounded-md border border-input bg-card/70 px-3 py-2 text-sm shadow-xs outline-none transition-[color,box-shadow,border-color] placeholder:text-muted-foreground/80 focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/30"
              />
              <div className="flex items-center justify-between gap-3">
                <span className="text-[0.68rem] text-muted-foreground">
                  {question.length}/{MAX_QUESTION} characters
                </span>
                <Button type="submit" size="sm" className="rounded-full" disabled={asking || !question.trim()}>
                  {asking ? (
                    <Loader2 className="size-4 animate-spin" aria-hidden="true" />
                  ) : (
                    <Send className="size-4" aria-hidden="true" />
                  )}
                  {asking ? 'Asking' : 'Ask'}
                </Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      <Button
        type="button"
        size="lg"
        className="rounded-full shadow-lg"
        onClick={() => setOpen((current) => !current)}
        aria-expanded={open}
        aria-label={open ? 'Hide the shop assistant' : 'Ask the shop assistant'}
      >
        {open ? (
          <X className="size-5" aria-hidden="true" />
        ) : (
          <MessageCircle className="size-5" aria-hidden="true" />
        )}
        <span className="hidden sm:inline">{open ? 'Hide assistant' : 'Ask the shop'}</span>
      </Button>
    </div>
  )
}
