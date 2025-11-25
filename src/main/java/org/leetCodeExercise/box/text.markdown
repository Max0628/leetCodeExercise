
# Chat System Design

---

## Functional Requirements

### User Management

#### Basic Requirement

- **Register & Login**
    1. Users can only register or login using Google account
    2. After login, the server issues:
         - **JWT Access Token** (valid 15 minutes)
         - **Refresh Token** (valid 30 days)
    3. Refresh Token can be used to get a new Access Token.
    4. After login, a WebSocket connection is established.

- **Friend System & User Profile**
    1. Users can edit their profile: `username`, `gender`, `password`, `avatar`, `introduction`
    2. Users can add friends by entering their friend’s ID:
         - A friend request is sent.
         - The other user must accept for the friendship to be established.
         - Friend requests have status: `pending`, `accepted`, `rejected`
    3. Users can check the online/offline status of friends:
         - websocket heartbeat?
         - redis presence?
         - last activity timestamp?
    4. Users can view friend’s profile info: `username`, `avatar`, `introduction`, `last online timestamp`
    5. Users can peek the last message exchanged with a friend on the dashboard.
    6. Users can see the number of unread messages per friend on the dashboard.
    7. Users can update their avatar and introduction, which syncs across all devices.

- **Common Chat Details**
    1. Every chat message has a timestamp: `client local time` + `server UTC time`
    2. Messages have read/unread status, synced across devices.
    3. Users can unsend messages within 1 hour:
         - A placeholder is left saying “This message was unsent.”
         - Status is synchronized across devices.

- **1 on 1 Chat**
    1. Users can start a one-on-one chat with any friend.
    2. All messages are delivered via WebSocket for real-time updates.

- **Group Chat**
    1. User can create a group chat.
    2. Users can join group chats via groupId invitation.
    3. Each group must support at least 100 members.
    4. Groups have roles: **Admin** and **Member**
         - Admins can kick users and rename the group.
    5. Group messages are delivered via WebSocket, with unread counts per member.

#### Advance Functions (Nice to Have)

- **Chat Related**
    - Users can search friends by username
    - Users can search chat history using keywords (full-text search via Elasticsearch or similar)
    - Multi-device Support (Sync across devices)
        - Messages, read/unread status, and friend list are synchronized across devices using WebSocket + Redis pub/sub + DB sync
    - Message encryption (end-to-end encryption)
        - Optional end-to-end encryption (E2EE)
        - Server stores encrypted messages only; cannot decrypt content

- **Notifications**
    - In-page notification
        - Real-time notification via WebSocket
        - Show unread message counts or highlight chat windows
        - Notifications disappear when the user closes the page
    - No system-level push notifications (messages won’t appear if the page is closed)

- **Media Sharing (Images, Videos, Files)**
    - Text message
    - Images (`jpg/png/gif`, max 5MB)
    - Videos (`mp4`, max 50MB)
    - Audio (`mp3/wav`, max 10MB)
    - Files (`pdf/docx`, max 10MB)
    - All media files are uploaded to CDN/S3; messages contain URLs only

---

### BackStage/Admin Panel

- **User Management** (Ban, Suspend Users)
- **Analytics Dashboard** (User Activity, Message Volume)

---

## Non-functional Requirements

- **Scalability**
    - Support at least 100,000 concurrent users.
    - Use Kafka pub/sub and WebSocket gateway for load distribution.
    - Multiple WebSocket pods can handle connections concurrently.

- **Low Latency**
    - Message delivery latency via WebSocket < 200ms
    - Use in-memory caching + Redis to reduce DB hits

- **High Availability (Minimal Downtime)**
    - Deploy across multiple regions and pods
    - Redis + DB master-slave or cluster configuration
    - SLA target ≥ 99.9%

- **Security**
    - HTTPS for all communication
    - JWT authentication for APIs and WebSocket
    - Optional E2EE for sensitive messages
    - Protect against XSS, CSRF, SQL Injection, and other common attacks

---

## Back of Envelope Estimation

- **Assumptions**
    - Concurrent users: 100,000
    - 1 on 1 chat average per user: 0.1 message/second
    - Group chat average per user: 0.05 message/second
    - Average message size: 1kb text
    - Websocket heartbeat: every 30 seconds (~10 bytes)

- **Message Traffic**
    - 1 on 1 chat

---

## API Design
## Data Modeling
## High Level Design
## Follow Up (How to scale?)

---

## Tech Stack

- **Frontend:** React + TypeScript for PC & mobile web application, RWD, will be host on AWS S3 + CloudFront
- **Backend:** Java Spring Boot,Maven, 
- **Database:**
    - RDBMS: PostgreSQL for relational data (user profiles, message metadata)
    - NoSQL: MongoDB(aws) or Cassandra(aws) for storing message history and media metadata
    - KV Store: MongoDB for fast access to user sessions and presence information
    - In-Memory Data Store: Redis for caching and real-time presence tracking
- **Messaging Queue:** Apache Kafka or Rabbit MQ for handling message delivery and notifications
    - Message Queue: rabbitmq for passing messages between server and redis for websocket session.
    - Message Queue: kafka for saving chat message to mongoDB asynchronously.
- **Real-time Communication:** WebSocket & STOMP for real-time messaging
- **Authentication:** JWT (JSON Web Tokens) for secure user authentication
- **Media Storage:** AWS S3 for storing media files and CDN(aws cloudfront) for content delivery
- **Push Notifications:** Firebase Cloud Messaging (FCM) for sending push notifications(maybe not necessary)
- **Deployment:** Docker and Kubernetes for containerization and orchestration(aws EKS)
- **Observability:**
    - Metrics: Prometheus for monitoring application performance
    - Logging: ELK Stack (Elasticsearch, Logstash, Kibana) for centralized logging
    - Tracing: Jaeger for distributed tracing of requests
- **CI/CD:** Jenkins or GitHub Actions for continuous integration and deployment
- **Load Balancing:** NGINX or AWS ELB for distributing incoming traffic
- **API Gateway:** Kong or AWS API Gateway for managing and routing API requests
- **Security:** TLS/SSL for secure data transmission, OAuth 2.0 for authorization

---

## Third Party API

- Google login
- Facebook login

---

## Project Structure

- MVC project
- Three layer structure (backend)
- Linting and code format tools
  - Java(google-java-format)
  - JavaScript/TypeScript (ESLint, Prettier)
- Monorepo project
  - project structure
  - frontend/ React project
  - backend / Java Spring boot project
  - Infrastructure as code / terraform, docker, k8s, etc.
  - showFlake uniqueId generator service
  - Worker services 
    - Presence / Online Status Worker
    - Push Notification Worker
    - sync websocket session to redis 
    - save chat message to mongoDB asynchronously
  - Observability / monitoring, logging, tracing, etc.
  - docs / design documents, api docs, etc.
  - 

---
