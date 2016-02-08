package net.donky.core.messaging.push.mock;

import net.donky.core.helpers.IdHelper;
import net.donky.core.messaging.push.logic.SimplePushData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Marcin Swierczek
 * 22/07/2015.
 * Copyright (C) Donky Networks Ltd. All rights reserved.
 */
public class MockSimplePushData extends SimplePushData {

    public MockSimplePushData(int numberOfButtons) {

        setMessageType("SimplePush");
        setMsgSentTimeStamp("2015-07-22T09:07:33.880Z");
        setSenderDisplayName("Donky");
        setBody("hi");
        setSenderInternalUserId(IdHelper.generateId());
        setSenderMessageId(IdHelper.generateId());
        setMessageId(IdHelper.generateId());

        Map<String, String> context = new HashMap<>();
        context.put("k1","v1");
        context.put("k2", "v2");
        setContextItems(context);

        setAvatarAssetId(null);
        setSentTimestamp("2015-07-22T09:07:33.880Z");
        setExpiryTimeStamp(null);

        if (numberOfButtons == 0) {
            setButtonSets(null);
        } else if (numberOfButtons == 1) {
            ButtonSet buttonSet = new MockButtonSet(IdHelper.generateId(), "Mobile", "interactionType", "Dismiss");
            List<ButtonSet> buttonSets = new LinkedList<>();
            buttonSets.add(buttonSet);
            setButtonSets(buttonSets);
        } else if (numberOfButtons == 2) {
            ButtonSet buttonSet = new MockButtonSet(IdHelper.generateId(), "Mobile", "interactionType", "Dismiss", "Agree");
            List<ButtonSet> buttonSets = new LinkedList<>();
            buttonSets.add(buttonSet);
            setButtonSets(buttonSets);
        }

    }

    public class MockButtonSet extends ButtonSet {

        MockButtonSet(String buttonSetId, String platform, String interactionType, String buttonActionType) {
            setButtonSetId(buttonSetId);
            ButtonSetAction[] actions = new ButtonSetAction[1];
            actions[0] = new MockButtonSetAction(buttonActionType);
            setButtonSetActions(actions);
            setPlatform(platform);
            setInteractionType(interactionType);
        }

        MockButtonSet(String buttonSetId, String platform, String interactionType, String buttonActionTypeA, String buttonActionTypeB) {
            setButtonSetId(buttonSetId);
            ButtonSetAction[] actions = new ButtonSetAction[2];
            actions[0] = new MockButtonSetAction(buttonActionTypeA);
            actions[1] = new MockButtonSetAction(buttonActionTypeB);
            setButtonSetActions(actions);
            setPlatform(platform);
            setInteractionType(interactionType);
        }
    }

    public class MockButtonSetAction extends ButtonSetAction {

        MockButtonSetAction(String action) {
            setActionType(action);
        }
    }

}
