import {Component} from "@angular/core";
import {Headers, Http, URLSearchParams} from "@angular/http";
import {MessageLogEntry} from "../messagelog/messagelogentry";
import {isNullOrUndefined} from "util";
import {AlertService} from "../alert/alert.service";

@Component({
  moduleId: module.id,
  templateUrl: 'testservice.component.html',
  providers: []
})

export class TestServiceComponent {

  static readonly TEST_SERVICE_PARTIES_URL: string = 'rest/testservice/parties';
  static readonly TEST_SERVICE_SENDER_URL: string = 'rest/testservice/sender';
  static readonly TEST_SERVICE_SUBMIT_URL: string = 'rest/testservice/submit';
  static readonly TEST_SERVICE_SUBMIT_DYNAMICDISCOVERY_URL: string = 'rest/testservice/submitDynamicDiscovery';
  static readonly MESSAGE_LOG_LAST_TEST_SENT_URL: string = 'rest/messagelog/lastTestSent';
  static readonly MESSAGE_LOG_LAST_TEST_RECEIVED_URL: string = 'rest/messagelog/lastTestReceived';

  dynamicDiscoveryEnabled: boolean;

  receiverParties: Array<string> = [];

  filter: any = {};

  messageInfoSent: MessageLogEntry;
  messageInfoReceived: MessageLogEntry;

  buttonDisabled: boolean = true;

  sender : string = "";

  private headers = new Headers({'Content-Type': 'application/json'});

  constructor(private http: Http,
              private alertService: AlertService) {
    this.clearInfo();
    this.getReceiverParties();
    this.getSenderParty();
  }

  getSenderParty() {
    this.http.get(TestServiceComponent.TEST_SERVICE_SENDER_URL).subscribe( res => {
      this.sender = res.json();
    })
  }

  test() {
    this.clearInfo();
    if(this.isPModeDaoOrCachingPModeProvider()) {
      this.http.post(TestServiceComponent.TEST_SERVICE_SUBMIT_URL,
        JSON.stringify({
          sender: this.sender,
          receiver: this.filter.receiverPartyId
        }), {
          headers: this.headers
        }).subscribe(() => {
          this.onChangeParties();
        },
        error => {
          this.alertService.error("Problems while submitting test");
        });
    } else if (this.isDynamicDiscoveryPModeProvider()) {
      this.http.post(TestServiceComponent.TEST_SERVICE_SUBMIT_DYNAMICDISCOVERY_URL,
        JSON.stringify( {
          sender: this.sender,
          receiver: this.filter.finalRecipient,
          receiverType: this.filter.finalRecipientType,
          serviceType: this.filter.serviceType
        }), {
          headers: this.headers
        }).subscribe( () => {
          this.onChangeInfo();
      },
        error => {
          this.alertService.error("Problems while submitting test");
        });
    }
  }

  private isPModeDaoOrCachingPModeProvider() {
    return (!isNullOrUndefined(this.filter.receiverPartyId) && (this.filter.receiverPartyId.length > 0));
  }

  private isDynamicDiscoveryPModeProvider() {
    return (!isNullOrUndefined(this.filter.finalRecipient) && (this.filter.finalRecipient.length > 0) &&
      !isNullOrUndefined(this.filter.finalRecipientType) && (this.filter.finalRecipientType.length > 0) &&
      !isNullOrUndefined(this.filter.serviceType) && (this.filter.serviceType.length > 0));
  }

  private disableButtonAndClearInfo() {
    this.buttonDisabled = true;
    this.clearInfo();
  }

  onChangeParties() {
    if(this.isPModeDaoOrCachingPModeProvider()) {
      this.buttonDisabled = false;
      this.clearInfo();
      this.getLastSentRequest(this.filter.receiverPartyId);
    } else {
      this.disableButtonAndClearInfo();
    }
  }

  onChangeInfo() {
    if(this.isDynamicDiscoveryPModeProvider()) {
      this.buttonDisabled = false;
      this.clearInfo();
      //this.getLastSentRequest(this.filter.finalRecipient);
    } else {
      this.disableButtonAndClearInfo();
    }
  }

  find() {
    if(this.isDynamicDiscoveryPModeProvider()) {
      this.getLastSentRequest(this.filter.finalRecipient);
    } else {
      this.disableButtonAndClearInfo();
    }
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
      if(!isNullOrUndefined(res.json())) {
        this.alertService.clearAlert();
        this.messageInfoSent.toPartyId = res.json().partyId;
        this.messageInfoSent.finalRecipient = res.json().accessPoint;
        this.messageInfoSent.receivedTo = new Date(res.json().timeReceived);
        this.messageInfoSent.messageId = res.json().messageId;

        this.getLastReceivedRequest(partyId, res.json().messageId);
      }
    }, error => {
      this.alertService.error("PartyId '"+ partyId + "' not found!");
    });
  }

  getLastReceivedRequest(partyId: string, userMessageId: string) {
    let searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('partyId', partyId);
    searchParams.set('userMessageId', userMessageId);
    this.http.get(TestServiceComponent.MESSAGE_LOG_LAST_TEST_RECEIVED_URL, {search: searchParams}).subscribe( res => {
      if(!isNullOrUndefined(res.json())) {
        this.messageInfoReceived.fromPartyId = partyId;
        this.messageInfoReceived.originalSender = res.json().accessPoint;
        this.messageInfoReceived.receivedFrom = new Date(res.json().timeReceived);
        this.messageInfoReceived.messageId = res.json().messageId;
      }
    });
  }

}
