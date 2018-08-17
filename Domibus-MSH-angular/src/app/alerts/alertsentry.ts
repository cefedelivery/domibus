export class AlertsEntry {
  constructor(public processed: boolean,
              public alertId: string,
              public alertType: string,
              public alertLevel: string,
              public alertStatus: string,
              public attempts: number,
              public maxAttempts: number,
              public creationTime: Date,
              public reportingTime: Date,
              public reportingTimeFailure: Date,
              public nextAttempt: Date,
              public parameters: string[])
  {

  }
}
