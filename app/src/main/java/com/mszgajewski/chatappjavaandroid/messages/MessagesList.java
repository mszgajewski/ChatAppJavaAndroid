package com.mszgajewski.chatappjavaandroid.messages;

public class MessagesList {

    private String name, phone, lastMessage, profilePic;
    private int unseenMessages;

    public MessagesList(String name, String phone,String profilePic, String lastMessage, int unseenMessages) {
        this.name = name;
        this.phone = phone;
        this.profilePic = profilePic;
        this.lastMessage = lastMessage;
        this.unseenMessages = unseenMessages;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public int getUnseenMessages() {
        return unseenMessages;
    }
}
