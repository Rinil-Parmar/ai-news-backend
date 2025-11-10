# 📰 AI News Backend

A Spring Boot backend service that provides personalized news recommendations using GNews API and machine learning.

---

## 🚀 Features

- User authentication and preference management
- Real-time news fetching from GNews API
- AI-powered article summarization and recommendations
- User activity tracking for personalized feeds
- MongoDB data storage

---

## 🛠️ Tech Stack

- **Backend:** Spring Boot (Java 17+)
- **Database:** MongoDB
- **External APIs:** GNews API, FastAPI ML Service
- **Build Tool:** Maven

---

## 📡 API Endpoints

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - User login

### User Management
- `GET /users/me` - Get current user
- `PUT /users/me/preferences` - Update preferences

### News
- `POST /news/init` - Fetch news from GNews API
- `GET /news` - Get all news articles
- `GET /news/{id}` - Get article by ID
- `GET /news/english` - Get English articles

### ML Integration
- `POST /ml/summarize` - Summarize article
- `POST /ml/recommend` - Get recommendations

### User Events
- `POST /events` - Track user interaction
- `GET /events/user/{userId}` - Get user activity

---

## ⚙️ Setup

### Prerequisites
- Java 17+
- Maven
- MongoDB
- GNews API Key

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/ai-news-backend.git
cd ai-news-backend
```

2. **Configure application.properties**
```properties
gnews.api.key=YOUR_API_KEY
spring.data.mongodb.uri=mongodb://localhost:27017/ai-news
ml.service.url=http://localhost:8000
```

3. **Run the application**
```bash
mvn spring-boot:run
```

4. **Initialize news data**
```bash
curl -X POST http://localhost:8080/news/init
```

---

## 📁 Project Structure
```
src/main/java/
├── controller/    # REST endpoints
├── service/       # Business logic
├── model/         # Data models
├── repository/    # MongoDB repositories
└── config/        # Configuration
```

---

## 🏗️ Architecture
```
GNews API → Spring Boot Backend → FastAPI ML Service
                  ↓
              MongoDB
```

---

## 📝 Environment Variables
```bash
GNEWS_API_KEY=your_api_key
MONGODB_URI=mongodb://localhost:27017/ai-news
ML_SERVICE_URL=http://localhost:8000
```

---


## 👨‍💻 Author

- **Rinil Parmar**
- **Aditi Parmar**

---

**⭐ Star this repo if you find it helpful!**
