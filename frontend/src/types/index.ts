// Enums — matching backend Java enums exactly

export enum TopicArea {
  JAVA_CORE = 'JAVA_CORE',
  SPRING_BOOT = 'SPRING_BOOT',
  SYSTEM_DESIGN = 'SYSTEM_DESIGN',
  DSA = 'DSA',
  DATABASE = 'DATABASE',
  ARCHITECTURE = 'ARCHITECTURE',
  BEHAVIORAL = 'BEHAVIORAL',
}

export enum CardStatus {
  NEW = 'NEW',
  LEARNING = 'LEARNING',
  REVIEW = 'REVIEW',
  GRADUATED = 'GRADUATED',
}

export enum Difficulty {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD',
}

export enum QuestionType {
  MCQ = 'MCQ',
  FILL_BLANK = 'FILL_BLANK',
  PREDICT_OUTPUT = 'PREDICT_OUTPUT',
  EXPLAIN_CODE = 'EXPLAIN_CODE',
}

export enum ChallengeType {
  DSA = 'DSA',
  JAVA = 'JAVA',
  SQL = 'SQL',
}

export enum SubmissionStatus {
  RUNNING = 'RUNNING',
  PASSED = 'PASSED',
  FAILED = 'FAILED',
  TIMEOUT = 'TIMEOUT',
  ERROR = 'ERROR',
}

// Interfaces — matching backend DTOs

export interface SpringPage<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
}

export interface Topic {
  id: string;
  name: string;
  area: TopicArea;
  parentId: string | null;
  description: string | null;
  sortOrder: number;
  childCount: number;
  cardCount: number;
}

export interface Card {
  id: string;
  topicId: string;
  topicName: string;
  front: string;
  back: string;
  codeSnippet: string | null;
  explanation: string | null;
  difficulty: Difficulty;
  tags: string[];
  status: CardStatus;
  nextReview: string | null;
}

export interface CardReview {
  cardId: string;
  grade: number;
  reviewedAt: string;
}

export interface Guide {
  id: string;
  topicId: string;
  title: string;
  contentMarkdown: string;
  estimatedMinutes: number;
  questionCount: number;
}

export interface QuizQuestion {
  id: string;
  questionType: QuestionType;
  questionText: string;
  options: Record<string, string>[] | null;
  difficulty: Difficulty;
}

export interface Challenge {
  id: string;
  topicId: string;
  title: string;
  difficulty: Difficulty;
  challengeType: ChallengeType;
  solved: boolean;
}

export interface ChallengeDetail {
  id: string;
  title: string;
  problemStatement: string;
  difficulty: Difficulty;
  challengeType: ChallengeType;
  starterCode: string | null;
  hints: string[];
  timeLimitSeconds: number;
  submissions: Submission[];
  referenceSolution: string | null;
}

export interface Submission {
  id: string;
  status: SubmissionStatus;
  testResults: Record<string, unknown>[] | null;
  aiReview: string | null;
  executionTimeMs: number | null;
  code: string | null;
}

export interface DesignExercise {
  id: string;
  topicId: string;
  title: string;
  difficulty: Difficulty;
  estimatedMinutes: number;
}

export interface MockInterview {
  id: string;
  topicArea: TopicArea;
  difficulty: Difficulty;
  state: string;
  overallScore: number | null;
  categoryScores: Record<string, number> | null;
  feedback: string | null;
  startedAt: string;
  completedAt: string | null;
}

export interface InterviewTurn {
  turnNumber: number;
  aiQuestion: string;
  evaluation: Record<string, unknown> | null;
  phase: string;
  isComplete: boolean;
}

export interface InterviewSummary {
  id: string;
  topicArea: string;
  difficulty: string;
  state: string;
  startedAt: string;
  completedAt: string | null;
  turnCount: number;
  overallScore: number | null;
}

export interface TodayActivity {
  cardsReviewed: number;
  challengesSolved: number;
  quizzesCompleted: number;
  interviewsDone: number;
  studyMinutes: number;
}

export interface DailyActivity {
  activityDate: string;
  cardsReviewed: number;
  challengesSolved: number;
  quizzesCompleted: number;
  interviewsDone: number;
  studyMinutes: number;
}

export interface UserProgress {
  topicId: string;
  cardsMastered: number;
  cardsTotal: number;
  challengesSolved: number;
  challengesTotal: number;
  guidesCompleted: number;
  guidesTotal: number;
  lastActivity: string | null;
}

export interface ReviewSessionData {
  sessionId: string;
  cards: Card[];
  totalCards: number;
}

export interface DashboardData {
  dueCardCount: number;
  currentStreak: number;
  longestStreak: number;
  todayActivity: TodayActivity;
  weakAreas: string[];
  recentSessions: unknown[];
}

export interface StudyMessageDto {
  role: string;
  content: string;
  sequence: number;
}

export interface StudySessionDto {
  id: string;
  topicId: string;
  topicName: string;
  topicArea: string;
  startedAt: string;
  lastActivityAt: string;
  messageCount: number;
  messages: StudyMessageDto[] | null;
}

export interface StudySessionSummary {
  id: string;
  topicId: string;
  topicName: string;
  topicArea: string;
  startedAt: string;
  lastActivityAt: string;
  messageCount: number;
  preview: string;
}

export interface DailyRecommendation {
  reviewCards: Card[];
  dsaChallenges: Challenge[];
  designExercise: DesignExercise | null;
  motivationalMessage: string;
  revisionChallenges: Challenge[];
  behavioralPracticeRecommended: boolean;
}
