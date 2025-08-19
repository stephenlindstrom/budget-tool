// src/components/PennyPincherLogo.jsx
// Simplified logo: a coin with two stylized cursive "P" letters inside, copper penny colors.

export default function PennyPincherLogo({
  size = 64,                // icon height/width in px
  ringColor = "#B87333",     // copper outer ring
  faceColor = "#FFD8B1",     // lighter copper face
  textColor = "#7B3F00",     // dark brown/copper text
  showWordmark = true,       // toggle app name below/next to icon
  className = ""
}) {
  return (
    <div className={`inline-flex flex-col items-center gap-2 select-none ${className}`.trim()}>
      <svg
        xmlns="http://www.w3.org/2000/svg"
        viewBox="0 0 100 100"
        width={size}
        height={size}
        role="img"
        aria-label="Penny Pincher logo"
      >
        {/* Coin shape */}
        <circle cx="50" cy="50" r="48" fill={ringColor} />
        <circle cx="50" cy="50" r="40" fill={faceColor} />

        {/* Two stylized cursive Ps */}
        <text
          x="50%"
          y="50%"
          dominantBaseline="middle"
          textAnchor="middle"
          fontSize="34"
          fontFamily="'Brush Script MT', cursive"
          fill={textColor}
        >
          PP
        </text>
      </svg>

      {showWordmark && (
        <span className="font-semibold tracking-tight" style={{ color: textColor, fontFamily: "'Brush Script MT', cursive" }}>
          Penny Pincher
        </span>
      )}
    </div>
  );
}

// Usage examples:
// <PennyPincherLogo size={80} />
// <PennyPincherLogo size={40} showWordmark={false} />
