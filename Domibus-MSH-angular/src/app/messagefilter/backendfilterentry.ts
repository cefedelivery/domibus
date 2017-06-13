import {RoutingCriteriaEntry} from "./routingcriteriaentry";


export class BackendFilterEntry {

  from: RoutingCriteriaEntry;
  to: RoutingCriteriaEntry;
  action: RoutingCriteriaEntry;
  sevice: RoutingCriteriaEntry ;



  public constructor(public entityId: number,
                     public index: number,
                     public backendName: string,
                     public routingCriterias: Array<RoutingCriteriaEntry>) {
    this.initRoutingCriteria();
  }

  public initRoutingCriteria() {
    this.from = this.getRoutingCriteria("from");
    this.to = this.getRoutingCriteria("to");
    this.action = this.getRoutingCriteria("action");
    this.sevice = this.getRoutingCriteria("sevice");
  }

  public getRoutingCriteria(property: string): RoutingCriteriaEntry {
    for (let i = 0; i < this.routingCriterias.length; i++) {
      if (this.routingCriterias[i].name == property) {
        return this.routingCriterias[i];
      }
    }
    return null;
  }

}
