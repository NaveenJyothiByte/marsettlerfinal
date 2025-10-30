import java.time.LocalDateTime;
import java.util.*;

public class CommunicationsService {
    private static class Message {
        public final String messageId;
        public final String fromUser;
        public final String toUser;
        public final String subject;
        public final String content;
        public final LocalDateTime sentTime;
        public LocalDateTime receivedTime;
        public boolean isUrgent;
        public int transmissionDelay;
        
        public Message(String messageId, String fromUser, String toUser, 
                      String subject, String content, boolean isUrgent) {
            this.messageId = messageId;
            this.fromUser = fromUser;
            this.toUser = toUser;
            this.subject = subject;
            this.content = content;
            this.sentTime = LocalDateTime.now();
            this.isUrgent = isUrgent;
            this.transmissionDelay = isUrgent ? 4 : 8 + (int)(Math.random() * 16);
        }
    }
    
    private final Map<String, Message> sentMessages = new HashMap<>();
    private final Map<String, List<Message>> receivedMessages = new HashMap<>();
    private final Map<String, List<Message>> pendingTransmission = new HashMap<>();
    private int nextMessageId = 1;
    
    public String sendMessage(String fromUser, String toUser, 
                             String subject, String content, boolean isUrgent) {
        String messageId = "MSG-" + (nextMessageId++);
        Message message = new Message(messageId, fromUser, toUser, subject, content, isUrgent);
        sentMessages.put(messageId, message);
        simulateTransmission(message);
        return messageId;
    }
    
    private void simulateTransmission(Message message) {
        message.receivedTime = message.sentTime.plusMinutes(message.transmissionDelay);
        pendingTransmission.computeIfAbsent(message.toUser, k -> new ArrayList<>()).add(message);
    }
    
    public List<Message> getReceivedMessages(String username) {
        List<Message> received = new ArrayList<>();
        List<Message> pending = pendingTransmission.getOrDefault(username, new ArrayList<>());
        Iterator<Message> iterator = pending.iterator();
        while (iterator.hasNext()) {
            Message message = iterator.next();
            if (LocalDateTime.now().isAfter(message.receivedTime)) {
                received.add(message);
                iterator.remove();
                receivedMessages.computeIfAbsent(username, k -> new ArrayList<>()).add(message);
            }
        }
        return received;
    }
    
    public String getMessageStatus(String messageId) {
        Message message = sentMessages.get(messageId);
        if (message == null) return "UNKNOWN_MESSAGE";
        if (message.receivedTime == null) {
            return "TRANSMITTING - Estimated delay: " + message.transmissionDelay + " minutes";
        }
        if (LocalDateTime.now().isBefore(message.receivedTime)) {
            long minutesRemaining = java.time.Duration.between(LocalDateTime.now(), message.receivedTime).toMinutes();
            return "IN_TRANSIT - Arriving in " + minutesRemaining + " minutes";
        }
        return "DELIVERED at " + message.receivedTime;
    }
    
    public void emergencyBroadcast(String fromUser, String subject, String content) {
        List<String> allUsers = Arrays.asList("resident.valid@mars.local", "mission.control@mars.local", "tech.team@mars.local");
        for (String user : allUsers) {
            sendMessage(fromUser, user, "[URGENT] " + subject, content, true);
        }
    }
}