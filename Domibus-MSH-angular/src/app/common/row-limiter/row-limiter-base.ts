export class RowLimiterBase {

  pageSizes: Array<any> = [
    {key: '2', value: 2},
    {key: '5', value: 5},
    {key: '50', value: 50},
    {key: '100', value: 100}
  ];

  pageSize: number = this.pageSizes[0].value;
}
