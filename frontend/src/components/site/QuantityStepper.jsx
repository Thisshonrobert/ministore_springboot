//member 3
import { Minus, Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'

export function QuantityStepper({ value, onChange, disabled = false, min = 1, max = 100 }) {
  return (
    <div className="inline-flex items-center rounded-full border border-border bg-background/70 p-1">
      <Button
        type="button"
        variant="ghost"
        size="icon"
        className="size-7 rounded-full"
        onClick={() => onChange(value - 1)}
        disabled={disabled || value <= min}
        aria-label="Decrease quantity"
      >
        <Minus className="size-3.5" aria-hidden="true" />
      </Button>
      <span className="min-w-8 text-center font-mono text-sm font-semibold" aria-live="polite">
        {value}
      </span>
      <Button
        type="button"
        variant="ghost"
        size="icon"
        className="size-7 rounded-full"
        onClick={() => onChange(value + 1)}
        disabled={disabled || value >= max}
        aria-label="Increase quantity"
      >
        <Plus className="size-3.5" aria-hidden="true" />
      </Button>
    </div>
  )
}
