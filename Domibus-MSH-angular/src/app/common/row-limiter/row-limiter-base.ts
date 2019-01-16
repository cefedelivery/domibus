export class RowLimiterBase {

  pageSizes: Array<any> = [
    {key: '10', value: 10},
    {key: '25', value: 25},
    {key: '50', value: 50},
    {key: '100', value: 100}
  ];

  pageSize: number = this.pageSizes[0].value;
}
