create table tb_UI_message as

(



select

                         usermessag0_.MESSAGE_ID,

                         usermessag0_.MESSAGE_STATUS,

                         usermessag0_.NOTIFICATION_STATUS,

                         usermessag0_.MSH_ROLE,

                         usermessag0_.MESSAGE_TYPE,

                         usermessag0_.DELETED,

                         usermessag0_.RECEIVED,

                         usermessag0_.SEND_ATTEMPTS,

                         usermessag0_.SEND_ATTEMPTS_MAX,

                         usermessag0_.NEXT_ATTEMPT,

                         usermessag0_.FAILED,

                         usermessag0_.RESTORED,



                         usermessag1_.COLL_INFO_CONVERS_ID,

                         partyid5_.VALUE as from_id,

                         partyid6_.VALUE as to_id,



                         property3_.VALUE as from_scheme,

                         property4_.VALUE as to_scheme,



                         messageinf2_.REF_TO_MESSAGE_ID



                     from

                         TB_MESSAGE_LOG usermessag0_



                     left outer join

                         TB_MESSAGE_INFO messageinf2_

                             on usermessag0_.MESSAGE_ID=messageinf2_.MESSAGE_ID,





                    TB_USER_MESSAGE usermessag1_



                     left outer join

                         TB_PROPERTY property3_

                             on usermessag1_.ID_PK=property3_.MESSAGEPROPERTIES_ID

                     left outer join

                         TB_PROPERTY property4_

                             on usermessag1_.ID_PK=property4_.MESSAGEPROPERTIES_ID

                     left outer join

                         TB_PARTY_ID partyid5_

                             on usermessag1_.ID_PK=partyid5_.FROM_ID

                     left outer join

                         TB_PARTY_ID partyid6_

                             on usermessag1_.ID_PK=partyid6_.TO_ID





                     where

                         usermessag1_.messageInfo_ID_PK=messageinf2_.ID_PK

                         and property3_.NAME='originalSender'

                         and property4_.NAME='finalRecipient'

--                       and partyid5_.VALUE= 'domibus'



--                     order by

--                         usermessag0_.RECEIVED desc



)

alter table tb_ui_message add column `ID_PK` int(10) unsigned primary KEY AUTO_INCREMENT;

