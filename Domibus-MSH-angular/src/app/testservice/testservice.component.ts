import {Component} from "@angular/core";
import {Http} from "@angular/http";

@Component({
  moduleId: module.id,
  templateUrl: 'testservice.component.html',
  providers: []
})

export class TestServiceComponent {

  static readonly TEST_SERVICE_PARTIES_URL: string = 'rest/testservice/parties';

  dynamicDiscoveryEnabled: boolean;

  receiverParties: Array<string> = [];

  filter: any = {};

  constructor(private http: Http) {
    this.getReceiverParties();
  }

  test() {

  }

  getReceiverParties() {
    this.http.get(TestServiceComponent.TEST_SERVICE_PARTIES_URL).subscribe( res => {
      this.receiverParties = res.json();
      this.dynamicDiscoveryEnabled = this.receiverParties.length == 0;
    });
  }

}
