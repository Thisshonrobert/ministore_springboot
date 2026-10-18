import { Toaster as Sonner } from 'sonner'

function Toaster({ ...props }) {
  return (
    <Sonner
      theme="light"
      className="toaster group"
      position="top-right"
      offset={{ top: 118, right: 24 }}
      mobileOffset={{ top: 112, right: 12 }}
      duration={4000}
      closeButton
      toastOptions={{
        classNames: {
          toast:
            'group toast group-[.toaster]:border-border group-[.toaster]:bg-card group-[.toaster]:text-card-foreground group-[.toaster]:shadow-lg group-[.toaster]:font-sans',
          description: 'group-[.toast]:text-muted-foreground',
          actionButton:
            'group-[.toast]:bg-primary group-[.toast]:text-primary-foreground group-[.toast]:font-semibold',
          cancelButton:
            'group-[.toast]:bg-muted group-[.toast]:text-muted-foreground group-[.toast]:font-semibold',
          success: 'group-[.toaster]:text-foreground',
          error: 'group-[.toaster]:text-foreground',
        },
      }}
      {...props}
    />
  )
}

export { Toaster }
