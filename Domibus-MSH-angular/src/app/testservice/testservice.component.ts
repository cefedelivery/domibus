import {Component} from "@angular/core";
import {Http, URLSearchParams} from "@angular/http";
import {MessageLogEntry} from "../messagelog/messagelogentry";
import {isNullOrUndefined} from "util";

@Component({
  moduleId: module.id,
  templateUrl: 'testservice.component.html',
  providers: []
})

export class TestServiceComponent {

  static readonly TEST_SERVICE_PARTIES_URL: string = 'rest/testservice/parties';
  static readonly MESSAGE_LOG_LAST_TEST_SENT_URL: string = 'rest/messagelog/lastTestSent';
  static readonly MESSAGE_LOG_LAST_TEST_RECEIVED_URL: string = 'rest/messagelog/lastTestReceived';

  dynamicDiscoveryEnabled: boolean;

  receiverParties: Array<string> = [];

  filter: any = {};

  messageInfoSent: MessageLogEntry;
  messageInfoReceived: MessageLogEntry;

  constructor(private http: Http) {
    this.clearInfo();
    this.getReceiverParties();
  }

  test() {

  }

  onChangeParties() {
    this.clearInfo();
    this.getLastSentRequest(this.filter.receiverPartyId);
  }

  clearInfo() {
    this.messageInfoSent = new MessageLogEntry('','','','','','','','','','','', null, null, false);
    this.messageInfoReceived = new MessageLogEntry('','','','','','','','','','','', null, null, false);
  }

  getReceiverParties() {
    this.http.get(TestServiceComponent.TEST_SERVICE_PARTIES_URL).subscribe( res => {
      if (!isNullOrUndefined(res)) {
        this.receiverParties = res.json();
      }
      this.dynamicDiscoveryEnabled = this.receiverParties.length == 0;
    });
  }

  getLastSentRequest(partyId: string) {
    let searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('partyId', partyId);
    this.http.get(TestServiceComponent.MESSAGE_LOG_LAST_TEST_SENT_URL, {search: searchParams}).subscribe( res => {
      this.messageInfoSent.toPartyId = res.json().partyId;
      this.messageInfoSent.finalRecipient = res.json().accessPoint;
      this.messageInfoSent.receivedTo = new Date(res.json().timeReceived);
      this.messageInfoSent.messageId = res.json().messageId;

      this.getLastReceivedRequest(partyId, res.json().messageId);
    });
  }

  getLastReceivedRequest(partyId: string, userMessageId: string) {
    let searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('partyId', partyId);
    searchParams.set('userMessageId', userMessageId);
    this.http.get(TestServiceComponent.MESSAGE_LOG_LAST_TEST_RECEIVED_URL, {search: searchParams}).subscribe( res => {
      this.messageInfoReceived.fromPartyId = partyId;
      this.messageInfoReceived.originalSender = res.json().accessPoint;
      this.messageInfoReceived.receivedFrom = new Date(res.json().timeReceived);
      this.messageInfoReceived.messageId = res.json().messageId;
    });
  }

}
