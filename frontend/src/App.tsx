import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from '@/components/Layout';
import Dashboard from '@/pages/Dashboard';
import TopicBrowser from '@/pages/TopicBrowser';
import ReviewSession from '@/pages/ReviewSession';
import GuidePage from '@/pages/GuidePage';
import QuizSession from '@/pages/QuizSession';
import ChallengeList from '@/pages/ChallengeList';
import ChallengeWorkspace from '@/pages/ChallengeWorkspace';
import DesignExerciseList from '@/pages/DesignExerciseList';
import DesignWorkspace from '@/pages/DesignWorkspace';
import MockInterviewSession from '@/pages/MockInterviewSession';
import InterviewHistory from '@/pages/InterviewHistory';
import StudyLanding from '@/pages/StudyLanding';
import StudySession from '@/pages/StudySession';
import StudyHistory from '@/pages/StudyHistory';
import BehavioralPage from '@/pages/BehavioralPage';
import Progress from '@/pages/Progress';

function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/topics" element={<TopicBrowser />} />
          <Route path="/review" element={<ReviewSession />} />
          <Route path="/guides/:id" element={<GuidePage />} />
          <Route path="/quiz/:id" element={<QuizSession />} />
          <Route path="/challenges" element={<ChallengeList />} />
          <Route path="/challenges/:id" element={<ChallengeWorkspace />} />
          <Route path="/exercises" element={<DesignExerciseList />} />
          <Route path="/exercises/:id" element={<DesignWorkspace />} />
          <Route path="/interviews/history" element={<InterviewHistory />} />
          <Route path="/interviews/:id" element={<MockInterviewSession />} />
          <Route path="/study" element={<StudyLanding />} />
          <Route path="/study/history" element={<StudyHistory />} />
          <Route path="/study/session/:sessionId" element={<StudySession />} />
          <Route path="/study/:topicId" element={<StudySession />} />
          <Route path="/behavioral" element={<BehavioralPage />} />
          <Route path="/progress" element={<Progress />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}

export default App;
