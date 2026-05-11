import { useEffect } from 'react';
import { GameView } from './components/Game/GameView';
import './App.css';

function App() {
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('platformToken');
    const returnUrl = params.get('returnUrl');

    if (token) {
      sessionStorage.setItem('platformToken', token);
    }
    if (returnUrl) {
      sessionStorage.setItem('platformReturnUrl', returnUrl);
    }

    if (token || returnUrl) {
      window.history.replaceState({}, '', window.location.pathname);
    }
  }, []);

  return <GameView />;
}

export default App;
