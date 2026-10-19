//member 4
/**
 * Matches the small amount of Markdown the model uses: ***bold italic***,
 * **bold**, *italic* and `code`. Emphasis must hug real text, so a lone
 * asterisk used as multiplication (2 * 3) is left alone.
 */
const INLINE_PATTERN =
  /(\*\*\*[^*\n]+\*\*\*|\*\*(?!\s)[^*\n]*[^\s*]\*\*|\*(?!\s)[^*\n]*[^\s*]\*|`[^`\n]+`)/g

/**
 * Drops emphasis markers the model left unmatched, so no asterisks reach the
 * page. Paired markers are handled by `inline`; this is the safety net for a
 * reply such as "**Notebook: a ruled notebook" with no closing marker.
 */
function plainText(value) {
  return value
    .replace(/\*{2,}/g, '')
    .replace(/\*+(?=\S)/g, '')
    .replace(/(\S)\*+/g, '$1')
}

/** Renders the inline emphasis the assistant uses. */
function inline(value) {
  const parts = []
  let lastIndex = 0
  let key = 0

  for (const match of value.matchAll(INLINE_PATTERN)) {
    if (match.index > lastIndex) {
      parts.push(plainText(value.slice(lastIndex, match.index)))
    }

    const token = match[0]
    if (token.startsWith('***')) {
      parts.push(
        <strong key={key++}>
          <em>{plainText(token.slice(3, -3))}</em>
        </strong>,
      )
    } else if (token.startsWith('**')) {
      parts.push(<strong key={key++}>{plainText(token.slice(2, -2))}</strong>)
    } else if (token.startsWith('`')) {
      parts.push(
        <code key={key++} className="rounded bg-card/80 px-1 py-0.5 font-mono text-[0.8em]">
          {token.slice(1, -1)}
        </code>,
      )
    } else {
      parts.push(<em key={key++}>{plainText(token.slice(1, -1))}</em>)
    }

    lastIndex = match.index + token.length
  }

  if (lastIndex < value.length) {
    parts.push(plainText(value.slice(lastIndex)))
  }

  return parts
}

/**
 * Turns a short Markdown-style AI reply into paragraphs, headings and lists.
 * Without this the browser collapses the line breaks and prints the emphasis
 * asterisks literally, which made the answers hard to read.
 */
export function AiText({ text, className }) {
  const blocks = []
  let paragraph = []
  let list = null

  const flushParagraph = () => {
    if (paragraph.length > 0) {
      blocks.push({ type: 'paragraph', text: paragraph.join(' ') })
      paragraph = []
    }
  }

  const flushList = () => {
    if (list) {
      blocks.push(list)
      list = null
    }
  }

  for (const rawLine of String(text || '')
    .replace(/\r\n/g, '\n')
    .split('\n')) {
    const line = rawLine.trim()

    if (!line) {
      flushParagraph()
      flushList()
      continue
    }

    const heading = line.match(/^#{1,6}\s+(.*)$/)
    if (heading) {
      flushParagraph()
      flushList()
      blocks.push({ type: 'heading', text: heading[1] })
      continue
    }

    const bullet = line.match(/^[-*•]\s+(.*)$/)
    if (bullet) {
      flushParagraph()
      if (!list || list.type !== 'bullet') {
        flushList()
        list = { type: 'bullet', items: [] }
      }
      list.items.push(bullet[1])
      continue
    }

    const numbered = line.match(/^\d+[.)]\s+(.*)$/)
    if (numbered) {
      flushParagraph()
      if (!list || list.type !== 'numbered') {
        flushList()
        list = { type: 'numbered', items: [] }
      }
      list.items.push(numbered[1])
      continue
    }

    flushList()
    paragraph.push(line)
  }

  flushParagraph()
  flushList()

  return (
    <div className={className ? `space-y-2 ${className}` : 'space-y-2'}>
      {blocks.map((block, index) => {
        if (block.type === 'bullet' || block.type === 'numbered') {
          const ListTag = block.type === 'bullet' ? 'ul' : 'ol'
          return (
            <ListTag
              key={index}
              className={
                block.type === 'bullet'
                  ? 'list-disc space-y-1 pl-4 marker:text-accent'
                  : 'list-decimal space-y-1 pl-4 marker:text-accent'
              }
            >
              {block.items.map((item, itemIndex) => (
                <li key={itemIndex}>{inline(item)}</li>
              ))}
            </ListTag>
          )
        }

        if (block.type === 'heading') {
          return (
            <p key={index} className="font-semibold text-foreground">
              {inline(block.text)}
            </p>
          )
        }

        return <p key={index}>{inline(block.text)}</p>
      })}
    </div>
  )
}
