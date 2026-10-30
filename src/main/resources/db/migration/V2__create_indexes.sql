CREATE INDEX idx_messages_conversation_id_created_at ON messages(conversation_id, created_at DESC);
CREATE INDEX idx_conversations_members_user_id ON conversation_members(user_id);
CREATE INDEX idx_notifications_user_id_is_read_created_at ON notifications(user_id, is_read, created_at);