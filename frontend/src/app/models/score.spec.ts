import {Score} from './score';

describe('Score', () => {
  it('exposes all enum keys and values', () => {
    expect(Score.getKeys()).toHaveSize(8);
    expect(Score.toArray()).toContain(Score.FUSEN_GACHI);
    expect(Score.getByKey('MEN')).toBe('MEN');
    expect(Score.getByKey('UNKNOWN')).toBeUndefined();
    expect(Score.getEnumKeyByEnumValue(Score as any, 'KOTE')).toBe('KOTE');
    expect(Score.getEnumKeyByEnumValue(Score as any, 'UNKNOWN')).toBeNull();
  });

  it('returns the scores used to clear or void a duel', () => {
    expect(Score.noCompetitor()).toEqual([Score.FUSEN_GACHI, Score.EMPTY]);
    expect(Score.clear()).toEqual([Score.EMPTY]);
  });

  it('formats tags, labels and camel case names', () => {
    expect(Score.tag(Score.MEN)).toBe('M');
    expect(Score.tag(Score.KOTE)).toBe('K');
    expect(Score.tag(Score.DO)).toBe('D');
    expect(Score.tag(Score.TSUKI)).toBe('T');
    expect(Score.tag(Score.HANSOKU)).toBe('H');
    expect(Score.tag(Score.IPPON)).toBe('I');
    expect(Score.tag(Score.FUSEN_GACHI)).toBe(' ');
    expect(Score.tag(undefined as unknown as Score)).toBe('');
    expect(Score.label(Score.FUSEN_GACHI)).toBe('Fusen_gachi');
    expect(Score.toCamel(Score.FUSEN_GACHI)).toBe('fusenGachi');
  });
});
