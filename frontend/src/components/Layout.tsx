import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  BookOpen,
  RefreshCw,
  Zap,
  Target,
  Mic,
  TrendingUp,
  Flame,
  Users,
} from 'lucide-react';
import type { LucideIcon } from 'lucide-react';

interface LayoutProps {
  children: React.ReactNode;
}

interface NavItem {
  path: string;
  label: string;
  icon: LucideIcon;
}

const navItems: NavItem[] = [
  { path: '/', label: 'Dashboard', icon: LayoutDashboard },
  { path: '/topics', label: 'Topics', icon: BookOpen },
  { path: '/review', label: 'Review', icon: RefreshCw },
  { path: '/challenges', label: 'Coding', icon: Zap },
  { path: '/exercises', label: 'Design', icon: Target },
  { path: '/interviews/new', label: 'Interviews', icon: Mic },
  { path: '/behavioral', label: 'Behavioral', icon: Users },
  { path: '/progress', label: 'Progress', icon: TrendingUp },
];

function Layout({ children }: LayoutProps) {
  return (
    <div className="flex h-screen overflow-hidden bg-[#06090f]">
      {/* Sidebar */}
      <aside className="hidden md:flex w-56 flex-col bg-surface-1 shadow-md">
        {/* Logo */}
        <div className="px-5 py-6">
          <h1 className="font-outfit text-xl font-bold text-gradient">
            InterviewKata
          </h1>
          <p className="text-xs text-content-muted mt-1">Discipline meets mastery</p>
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-3 space-y-1">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `relative flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-all duration-200 ${
                    isActive
                      ? 'bg-primary-500/10 text-primary-400 font-medium'
                      : 'text-content-secondary hover:text-content-primary hover:bg-surface-2/50'
                  }`
                }
              >
                {({ isActive }) => (
                  <>
                    {isActive && (
                      <span className="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-5 bg-primary-500 rounded-r-full" />
                    )}
                    <Icon size={18} strokeWidth={isActive ? 2 : 1.5} />
                    <span>{item.label}</span>
                  </>
                )}
              </NavLink>
            );
          })}
        </nav>

        {/* Sidebar footer — streak badge */}
        <div className="px-5 py-4">
          <div className="flex items-center gap-2 px-3 py-2 rounded-lg bg-primary-500/10">
            <Flame size={14} className="text-primary-400" />
            <span className="text-xs font-medium text-primary-400">
              Streak active
            </span>
          </div>
          <p className="text-xs text-content-muted mt-3 px-1">v0.1.0</p>
        </div>
      </aside>

      {/* Mobile sidebar — icons only */}
      <aside className="flex md:hidden w-14 flex-col items-center bg-surface-1 shadow-md py-4 gap-1">
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.path}
              to={item.path}
              className={({ isActive }) =>
                `relative flex items-center justify-center w-10 h-10 rounded-lg transition-all duration-200 ${
                  isActive
                    ? 'bg-primary-500/10 text-primary-400'
                    : 'text-content-secondary hover:text-content-primary hover:bg-surface-2/50'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  {isActive && (
                    <span className="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-4 bg-primary-500 rounded-r-full" />
                  )}
                  <Icon size={18} strokeWidth={isActive ? 2 : 1.5} />
                </>
              )}
            </NavLink>
          );
        })}
      </aside>

      {/* Main content area */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Subtle top bar */}
        <header className="h-12 flex items-center px-8 bg-surface-1/50 backdrop-blur-sm border-b border-border">
          <div className="flex-1" />
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-8">
          <div className="max-w-7xl mx-auto animate-fade-in">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}

export default Layout;
