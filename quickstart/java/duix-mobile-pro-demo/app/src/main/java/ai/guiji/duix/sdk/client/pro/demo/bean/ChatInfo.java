package ai.guiji.duix.sdk.client.pro.demo.bean;

/**
 * 对话信息
 */
public class ChatInfo {

    public static final int TYPE_BOT = 0;
    public static final int TYPE_USER = 1;

    private int type = TYPE_BOT;
    private String content = "";

    public ChatInfo(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ChatInfo{" +
                "type=" + type +
                ", content='" + content + '\'' +
                '}';
    }
}
