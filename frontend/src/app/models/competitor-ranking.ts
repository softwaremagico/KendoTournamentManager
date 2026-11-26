export class CompetitorRanking {
  ranking: number;
  total: number;

  get percentage(): number {
    if (this.total > 0) {
      return this.ranking / this.total;
    }
    return 0;
  }
}
