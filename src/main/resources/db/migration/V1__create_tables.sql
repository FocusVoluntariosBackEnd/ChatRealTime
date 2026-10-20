CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE conversations (
    id UUID PRIMARY KEY,
    type VARCHAR(30) NOT NULL CHECK (type IN ('PRIVATE', 'GROUP')),
    name VARCHAR(255),
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversations_users FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE conversation_members (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN ('ADMIN', 'MEMBER')),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_conversation_members_conversations FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    CONSTRAINT fk_conversation_members_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_conversation_members_conversation_user UNIQUE (conversation_id, user_id)
);

CREATE TABLE messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL,
    sender_id UUID NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_messages_conversations FOREIGN KEY (conversation_id) REFERENCES conversations(id),
    CONSTRAINT fk_messages_users FOREIGN KEY (sender_id) REFERENCES users(id)
);

CREATE TABLE messages_read (
    id UUID PRIMARY KEY,
    message_id UUID NOT NULL,
    user_id UUID NOT NULL,
    read_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_messages_messages_read FOREIGN KEY (message_id) REFERENCES messages(id),
    CONSTRAINT fk_messages_read_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_messages_read_message_user UNIQUE (message_id, user_id)
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    message_id UUID NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_notifications_messages FOREIGN KEY (message_id) REFERENCES messages(id),
    CONSTRAINT uk_notifications_user_message UNIQUE (user_id, message_id)
);