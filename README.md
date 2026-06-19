# Spring Boot RAG Document Q&A System

A production-style **Retrieval-Augmented Generation (RAG)** based Document Q&A system built with **Java 21**, **Spring Boot**, **Apache PDFBox**, **Qdrant**, and **OpenRouter API**.

This project allows users to upload PDF documents, convert them into searchable vector embeddings, and ask natural language questions. The system retrieves relevant document chunks and sends only that context to an LLM, helping generate answers that are grounded in the uploaded documents.

---

## Overview

This application demonstrates how to build a backend RAG system for document-based question answering.

Users can:

- Upload PDF documents
- Extract text from PDFs
- Split text into smaller chunks
- Generate embeddings for each chunk
- Store embeddings in Qdrant vector database
- Ask questions in natural language
- Retrieve relevant chunks using semantic search
- Send retrieved context and question to OpenRouter LLM
- Receive concise, document-grounded answers

The system is designed to reduce hallucination by forcing the LLM to answer only from retrieved document context.

---

## Architecture Flow

```text
User uploads PDF
        |
        v
Spring Boot REST API
        |
        v
Apache PDFBox extracts text
        |
        v
Text is split into chunks
        |
        v
Embeddings are generated
        |
        v
Embeddings + metadata are stored in Qdrant
        |
        v
User asks a question
        |
        v
Question is converted into an embedding
        |
        v
Qdrant performs semantic search
        |
        v
Relevant chunks are retrieved
        |
        v
Prompt is built with context + question
        |
        v
OpenRouter LLM generates grounded answer
        |
        v
Answer is returned to user
```

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming language |
| Spring Boot | REST API and application framework |
| Apache PDFBox | PDF text extraction |
| Qdrant | Vector database for semantic search |
| OpenRouter API | LLM provider for answer generation |
| Docker | Running Qdrant locally |
| Maven | Build and dependency management |

---

## Project Structure

```text
springboot-rag-chatbot/
|
├── docker-compose.yml
├── pom.xml
├── README.md
|
├── src/
|   ├── main/
|   |   ├── java/
|   |   |   └── com/example/rag/
|   |   |       ├── api/
|   |   |       |   ├── DocumentController.java
|   |   |       |   ├── GlobalExceptionHandler.java
|   |   |       |   └── dto/
|   |   |       |       ├── AskRequest.java
|   |   |       |       ├── AskResponse.java
|   |   |       |       └── UploadResponse.java
|   |   |       |
|   |   |       ├── config/
|   |   |       |   ├── EmbeddingProperties.java
|   |   |       |   ├── OpenRouterProperties.java
|   |   |       |   ├── QdrantProperties.java
|   |   |       |   └── RagProperties.java
|   |   |       |
|   |   |       ├── llm/
|   |   |       |   ├── ChatClient.java
|   |   |       |   └── OpenRouterChatClient.java
|   |   |       |
|   |   |       ├── model/
|   |   |       |   ├── DocumentChunk.java
|   |   |       |   └── SearchResult.java
|   |   |       |
|   |   |       ├── service/
|   |   |       |   ├── DocumentIngestionService.java
|   |   |       |   ├── EmbeddingClient.java
|   |   |       |   ├── HashingEmbeddingClient.java
|   |   |       |   ├── PdfTextExtractor.java
|   |   |       |   ├── PromptBuilder.java
|   |   |       |   ├── QuestionAnsweringService.java
|   |   |       |   └── TextChunker.java
|   |   |       |
|   |   |       ├── vector/
|   |   |       |   ├── QdrantVectorStore.java
|   |   |       |   └── VectorStore.java
|   |   |       |
|   |   |       └── RagChatbotApplication.java
|   |   |
|   |   └── resources/
|   |       └── application.yml
|   |
|   └── test/
|       └── java/
|           └── com/example/rag/
```

---

## RAG Pipeline Explained

### 1. PDF Upload

The user uploads a PDF file using the `/upload` API. The backend receives the file and validates that it is a supported PDF document.

### 2. Text Extraction

Apache PDFBox reads the PDF and extracts plain text. This converts the document into a format that can be processed by the RAG pipeline.

### 3. Text Chunking

Large documents are split into smaller chunks. Chunking is important because LLMs and embedding models work better with focused pieces of text instead of very large documents.

### 4. Embedding Generation

Each text chunk is converted into a numerical vector called an embedding. Embeddings represent the meaning of the text and allow semantic search.

### 5. Vector Storage in Qdrant

The generated embeddings are stored in Qdrant along with metadata such as:

- Document ID
- Document name
- Chunk index
- Original chunk text

### 6. Question Processing

When a user asks a question, the question is also converted into an embedding.

### 7. Semantic Search

Qdrant compares the question embedding with stored document embeddings and returns the most relevant chunks.

### 8. Prompt Building

The system builds a prompt using:

- Retrieved document chunks
- User question
- Strict instructions to answer only from the provided context

### 9. LLM Answer Generation

The prompt is sent to OpenRouter API. The configured LLM generates a final answer based only on the retrieved document context.

### 10. Grounded Response

If the retrieved context is not enough, the system returns:

```text
I don't know based on the provided documents.
```

---

## API Endpoints

### Upload PDF

```http
POST /upload
```

Uploads a PDF document, extracts text, chunks it, generates embeddings, and stores them in Qdrant.

#### Request

```http
Content-Type: multipart/form-data
```

| Field | Type | Required | Description |
|---|---|---|---|
| file | PDF | Yes | PDF document to upload |

#### Example Response

```json
{
  "documentId": "7f0e7c5a-9a41-4f1e-8b0a-123456789abc",
  "fileName": "sample.pdf",
  "chunksStored": 18
}
```

### Ask Question

```http
POST /ask
```

Accepts a natural language question, retrieves relevant chunks from Qdrant, sends context to OpenRouter, and returns a grounded answer.

#### Request

```http
Content-Type: application/json
```

#### Example Request

```json
{
  "question": "What is the main topic of this document?",
  "topK": 5
}
```

#### Example Response

```json
{
  "answer": "The document mainly discusses retrieval-augmented generation and how it improves question answering using external document context.",
  "sources": [
    {
      "documentName": "sample.pdf",
      "chunkIndex": 2,
      "score": 0.82,
      "text": "Retrieval-augmented generation combines semantic search with language models..."
    }
  ]
}
```

---

## Setup Instructions

### Prerequisites

Make sure you have the following installed:

- Java 21
- Maven
- Docker
- OpenRouter API key

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/springboot-rag-chatbot.git
cd springboot-rag-chatbot
```

### 2. Start Qdrant with Docker

```bash
docker compose up -d
```

Qdrant will run at:

```text
http://localhost:6333
```

### 3. Configure Environment Variables

Set your OpenRouter API key:

#### Windows PowerShell

```powershell
$env:OPENROUTER_API_KEY="your-openrouter-api-key"
```

#### macOS/Linux

```bash
export OPENROUTER_API_KEY="your-openrouter-api-key"
```

### 4. Configure Application

Update `src/main/resources/application.yml` if needed:

```yaml
openrouter:
  base-url: https://openrouter.ai/api/v1
  api-key: ${OPENROUTER_API_KEY}
  model: openai/gpt-4o-mini

qdrant:
  base-url: http://localhost:6333
  collection-name: document_chunks

rag:
  default-top-k: 5
  max-context-chars: 7000
  min-relevance-score: 0.15
```

The LLM model can be changed without modifying business logic.

Examples:

```yaml
openrouter:
  model: openai/gpt-4o
```

```yaml
openrouter:
  model: anthropic/claude-3.5-sonnet
```

```yaml
openrouter:
  model: mistralai/mistral-large
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start at:

```text
http://localhost:8080
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

---

## Example Usage

### Upload a PDF

```bash
curl -X POST http://localhost:8080/upload \
  -F "file=@sample.pdf"
```

Windows PowerShell:

```powershell
curl.exe -X POST http://localhost:8080/upload `
  -F "file=@C:\path\sample.pdf"
```

### Ask a Question

```bash
curl -X POST http://localhost:8080/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Summarize the key points from the document.",
    "topK": 5
  }'
```

Windows PowerShell:

```powershell
curl.exe -X POST http://localhost:8080/ask `
  -H "Content-Type: application/json" `
  -d "{\"question\":\"Summarize the key points from the document.\",\"topK\":5}"
```

---

## Why This Project Matters

This project demonstrates core skills used in modern AI backend systems:

- Building REST APIs with Spring Boot
- Processing documents with Java
- Designing a RAG pipeline
- Using vector databases for semantic search
- Integrating with LLM APIs
- Preventing hallucination through grounded context
- Separating responsibilities with clean architecture
- Designing systems that can be extended for production use

It is suitable as a foundation for:

- Document search systems
- Internal knowledge base assistants
- Legal document Q&A
- HR policy assistants
- Research paper Q&A tools
- Customer support knowledge assistants

---

## Future Improvements

- Replace local embedding logic with a production embedding model
- Add authentication and authorization
- Store document metadata in PostgreSQL
- Add document delete and re-index APIs
- Add support for DOCX, TXT, and HTML files
- Add async document ingestion using message queues
- Add reranking for better retrieval quality
- Add streaming responses from OpenRouter
- Add user-specific document collections
- Add observability with logs, metrics, and tracing
- Add integration tests with Testcontainers
- Add Dockerfile for the Spring Boot application
- Deploy using Kubernetes or cloud platforms

---

## Production Considerations

For a production deployment, consider adding:

- API authentication
- Rate limiting
- Request validation
- File size limits
- Virus scanning for uploaded files
- Retry and timeout handling for external APIs
- Centralized logging
- Monitoring and alerting
- Secure secret management
- Backup strategy for Qdrant data

---

## License

This project is intended for learning, experimentation, and portfolio use.

---

## Author

Built as a production-style Spring Boot RAG backend project for document-based question answering.
