export class PModeResult {
  constructor(public id,
              public configurationDate: Date,
              public username: string,
              public description: string,
              public xml: any,
              public current: boolean) {

  }
}
