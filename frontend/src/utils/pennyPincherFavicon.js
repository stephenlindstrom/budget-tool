export function getPennyPincherFavicon({
  size = 64,
  ringColor = "#B87333",
  faceColor = "#FFD8B1",
  textColor = "#7B3F00"
  } = {}) {
  const svg = encodeURIComponent(`<?xml version='1.0' encoding='UTF-8'?>
  <svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100' width='${size}' height='${size}'>
  <circle cx='50' cy='50' r='48' fill='${ringColor}'/>
  <circle cx='50' cy='50' r='40' fill='${faceColor}'/>
  <text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' font-size='34' font-family='Brush Script MT, cursive' fill='${textColor}'>PP</text>
  </svg>`);
  return `data:image/svg+xml,${svg}`;
}