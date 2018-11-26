package eu.domibus.api.message.fragment;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public class MessageFragment {

    protected String groupId;

    protected Integer fragmentNumber;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Integer getFragmentNumber() {
        return fragmentNumber;
    }

    public void setFragmentNumber(Integer fragmentNumber) {
        this.fragmentNumber = fragmentNumber;
    }
}
