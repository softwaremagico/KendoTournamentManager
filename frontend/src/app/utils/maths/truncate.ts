export function truncate(number: number, decimals: number): number {
  return Math.floor(number * Math.pow(10, decimals)) / Math.pow(10, decimals);
}
