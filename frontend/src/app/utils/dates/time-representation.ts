/**
 * Representación textual de un tiempo en segundos para los tooltips de
 * puntuación y faltas. Devuelve `undefined` cuando no hay tiempo, igual que
 * hacían ambos componentes.
 */
export function getTimeRepresentation(seconds: number | undefined,
                                      translate: (key: string) => string): string | undefined {
  if (!seconds) {
    return undefined;
  }
  let remainingSeconds: number = seconds;
  const minutes: number = Math.floor(remainingSeconds / 60);
  remainingSeconds = remainingSeconds % 60;
  let text: string = "";
  if (minutes) {
    text += minutes + " " + translate('minutesAbbreviation') + " ";
  }
  if (remainingSeconds) {
    text += remainingSeconds + " " + translate('secondsAbbreviation') + " ";
  }
  return text;
}
