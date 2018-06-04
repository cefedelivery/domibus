export class MessageLogEntry {
  constructor(public messageId: string,
              mshRole: string,
              conversationId: string,
              messageType: string,
              messageStatus: string,
              notificationStatus: string,
              public fromPartyId: string,
              public toPartyId: string,
              public originalSender: string,
              public finalRecipient: string,
              refToMessageId: string,
              public receivedFrom: Date,
              public receivedTo: Date,
              isTestMessage: boolean
  ) {

  }
}
