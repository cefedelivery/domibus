import {Component, OnInit} from '@angular/core';
import {Headers, Http, URLSearchParams} from '@angular/http';
import {MessageLogEntry} from '../messagelog/messagelogentry';
import {isNullOrUndefined} from 'util';
import {AlertService} from '../alert/alert.service';

@Component({
  moduleId: module.id,
  templateUrl: 'testservice.component.html',
  styleUrls: ['testservice.component.css'],
  providers: []
})

export class TestServiceComponent implements OnInit {

  static readonly TEST_SERVICE_URL: string = 'rest/testservice';
  static readonly TEST_SERVICE_PARTIES_URL: string = TestServiceComponent.TEST_SERVICE_URL + '/parties';
  static readonly TEST_SERVICE_SENDER_URL: string = TestServiceComponent.TEST_SERVICE_URL + '/sender';
  static readonly TEST_SERVICE_SUBMIT_DYNAMICDISCOVERY_URL: string = TestServiceComponent.TEST_SERVICE_URL + '/dynamicdiscovery';

  static readonly MESSAGE_LOG_LAST_TEST_SENT_URL: string = 'rest/messagelog/test/outgoing/latest';
  static readonly MESSAGE_LOG_LAST_TEST_RECEIVED_URL: string = 'rest/messagelog/test/incoming/latest';

  dynamicDiscoveryEnabled: boolean;

  receiverParties: Array<string>;

  filter: any;

  messageInfoSent: MessageLogEntry;
  messageInfoReceived: MessageLogEntry;

  buttonDisabled: boolean;

  sender: string;

  constructor (private http: Http, private alertService: AlertService) {
  }

  ngOnInit () {
    this.filter = {};
    this.receiverParties = [];
    this.buttonDisabled = true;
    this.sender = '';

    this.dynamicDiscoveryEnabled = false; // only static is available for now

    this.clearInfo();
    this.getReceiverParties();
    this.getSenderParty();
  }

  test () {
    this.clearInfo();
    if (this.isPModeDaoOrCachingPModeProvider()) {
      const payload = {sender: this.sender, receiver: this.filter.receiverPartyId};
      this.http.post(TestServiceComponent.TEST_SERVICE_URL, payload)
        .subscribe(
          () => this.onChangeParties(),
          () => this.alertService.error('Problems while submitting test')
        );
    } else if (this.isDynamicDiscoveryPModeProvider()) {
      const payload = {sender: this.sender, receiver: this.filter.finalRecipient, receiverType: this.filter.finalRecipientType};
      this.http.post(TestServiceComponent.TEST_SERVICE_SUBMIT_DYNAMICDISCOVERY_URL, payload)
        .subscribe(
          () => this.onChangeInfo(),
          () => this.alertService.error('Problems while submitting test')
        );
    }
  }

  private isPModeDaoOrCachingPModeProvider () {
    return (!isNullOrUndefined(this.filter.receiverPartyId) && (this.filter.receiverPartyId.length > 0));
  }

  private isDynamicDiscoveryPModeProvider () {
    return (!isNullOrUndefined(this.filter.finalRecipient) && (this.filter.finalRecipient.length > 0) &&
      !isNullOrUndefined(this.filter.finalRecipientType) && (this.filter.finalRecipientType.length > 0));
  }

  private disableButtonAndClearInfo () {
    this.buttonDisabled = true;
    this.clearInfo();
  }

  onChangeParties () {
    if (this.isPModeDaoOrCachingPModeProvider()) {
      this.buttonDisabled = false;
      this.clearInfo();
      this.getLastSentRequest(this.filter.receiverPartyId);
    } else {
      this.disableButtonAndClearInfo();
    }
  }

  onChangeInfo () {
    if (this.isDynamicDiscoveryPModeProvider()) {
      this.buttonDisabled = false;
      this.clearInfo();
    } else {
      this.disableButtonAndClearInfo();
    }
  }

  update () {
    if (this.isDynamicDiscoveryPModeProvider()) {
      this.getLastSentRequest(this.filter.finalRecipient);
    } else if (this.isPModeDaoOrCachingPModeProvider()) {
      this.getLastSentRequest(this.filter.receiverPartyId);
    } else {
      this.disableButtonAndClearInfo();
    }
  }

  clearInfo () {
    this.messageInfoSent = new MessageLogEntry('', '', '', '', '', '', '', '', '', '', '', null, null, false);
    this.messageInfoReceived = new MessageLogEntry('', '', '', '', '', '', '', '', '', '', '', null, null, false);
  }

  getSenderParty () {
    this.sender = '';
    this.http.get(TestServiceComponent.TEST_SERVICE_SENDER_URL).subscribe(res => {
      this.sender = res.json();
    }, error => {
      this.alertService.exception('The test service is not properly configured.', error, false);
    });
  }

  getReceiverParties () {
    this.receiverParties = [];
    this.http.get(TestServiceComponent.TEST_SERVICE_PARTIES_URL).subscribe(res => {
      if (!isNullOrUndefined(res) && res.json() && res.json().length) {
        this.receiverParties = res.json();
      } else {
        this.alertService.error('The test service is not properly configured.', false);
      }
      // only static is enabled for now
      //this.dynamicDiscoveryEnabled = this.receiverParties.length == 0;
    }, error => {
      this.alertService.exception('The test service is not properly configured.', error, false);
    });
  }

  getLastSentRequest (partyId: string) {
    let searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('partyId', partyId);
    this.http.get(TestServiceComponent.MESSAGE_LOG_LAST_TEST_SENT_URL, {search: searchParams}).subscribe(res => {
      const result = res.json();
      if (!isNullOrUndefined(result)) {
        this.alertService.clearAlert();
        this.messageInfoSent.toPartyId = result.partyId;
        this.messageInfoSent.finalRecipient = result.accessPoint;
        this.messageInfoSent.receivedTo = new Date(result.timeReceived);
        this.messageInfoSent.messageId = result.messageId;

        this.getLastReceivedRequest(partyId, result.messageId);
      }
    }, () => {
      this.alertService.error(`No information found for Test Messages of PartyId '${partyId}'`);
    });
  }

  getLastReceivedRequest (partyId: string, userMessageId: string) {
    let searchParams: URLSearchParams = new URLSearchParams();
    searchParams.set('partyId', partyId);
    searchParams.set('userMessageId', userMessageId);
    this.http.get(TestServiceComponent.MESSAGE_LOG_LAST_TEST_RECEIVED_URL, {search: searchParams}).subscribe(res => {
      const result = res.json();
      if (!isNullOrUndefined(result)) {
        this.messageInfoReceived.fromPartyId = partyId;
        this.messageInfoReceived.originalSender = result.accessPoint;
        this.messageInfoReceived.receivedFrom = new Date(result.timeReceived);
        this.messageInfoReceived.messageId = result.messageId;
      }
    });
  }

}
