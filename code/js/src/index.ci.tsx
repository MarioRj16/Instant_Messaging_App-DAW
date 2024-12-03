// Entry point to be used by the CI process
import * as React from 'react';
import { createRoot } from 'react-dom/client';
import App from './App';


const root = createRoot(document.getElementById('root'));
root.render(<App />);