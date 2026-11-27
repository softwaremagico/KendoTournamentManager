import {DatatableColumn} from './datatable-column';

describe('DatatableColumn', () => {
  it('defaults to an auto-resizable width', () => {
    const column = new DatatableColumn('Name', 'name');

    expect(column.width).toBe(150);
    expect(column.canAutoResize).toBeTrue();
  });

  it('preserves an explicit width while allowing automatic resizing', () => {
    const column = new DatatableColumn('Id', 'id', true, 80);

    expect(column.width).toBe(80);
    expect(column.canAutoResize).toBeTrue();
  });
});
