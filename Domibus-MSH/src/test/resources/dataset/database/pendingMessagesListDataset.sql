INSERT INTO "TB_MESSAGE_INFO" ("ID_PK","MESSAGE_ID","REF_TO_MESSAGE_ID","TIME_STAMP") VALUES (1,'2809cef6-240f-4792-bec1-7cb300a34679@domibus.eu',NULL,'2016-02-11 12:57:19');
INSERT INTO "TB_MESSAGE_INFO" ("ID_PK","MESSAGE_ID","REF_TO_MESSAGE_ID","TIME_STAMP") VALUES (2,'78a1d578-0cc7-41fb-9f35-86a5b2769a14@domibus.eu',NULL,'2016-02-11 16:29:44');
INSERT INTO "TB_MESSAGE_INFO" ("ID_PK","MESSAGE_ID","REF_TO_MESSAGE_ID","TIME_STAMP") VALUES (3,'2bbc05d8-b603-4742-a118-137898a81de3@domibus.eu',NULL,'2016-02-11 16:30:00');

INSERT INTO "TB_USER_MESSAGE" ("ID_PK","COLLABORATION_INFO_ACTION","AGREEMENT_REF_PMODE","AGREEMENT_REF_TYPE","AGREEMENT_REF_VALUE","COLL_INFO_CONVERS_ID","SERVICE_TYPE","SERVICE_VALUE","MPC","FROM_ROLE","TO_ROLE","MESSAGEINFO_ID_PK")
VALUES (1,'TC1Leg1',NULL,NULL,NULL,'7318c713-a1a7-4dc7-8497-337d40d95d39','tc1','bdx:noprocess','http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC','http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator','http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder',1);

INSERT INTO "TB_USER_MESSAGE" ("ID_PK","COLLABORATION_INFO_ACTION","AGREEMENT_REF_PMODE","AGREEMENT_REF_TYPE","AGREEMENT_REF_VALUE","COLL_INFO_CONVERS_ID","SERVICE_TYPE","SERVICE_VALUE","MPC","FROM_ROLE","TO_ROLE","MESSAGEINFO_ID_PK")
VALUES (2,'TC1Leg1',NULL,NULL,NULL,'489c1e59-2f4b-4c15-b780-38fa81f1df0e','tc1','bdx:noprocess','http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC','http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator','http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder',2);

INSERT INTO "TB_USER_MESSAGE" ("ID_PK","COLLABORATION_INFO_ACTION","AGREEMENT_REF_PMODE","AGREEMENT_REF_TYPE","AGREEMENT_REF_VALUE","COLL_INFO_CONVERS_ID","SERVICE_TYPE","SERVICE_VALUE","MPC","FROM_ROLE","TO_ROLE","MESSAGEINFO_ID_PK")
VALUES (3,'TC1Leg1',NULL,NULL,NULL,'9985e5cd-b898-4a7e-acd8-5fdf7a9edde7','tc1','bdx:noprocess','http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/defaultMPC','http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator','http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder',3);

