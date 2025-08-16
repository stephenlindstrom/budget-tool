import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { AuthProvider } from './context/AuthProvider.jsx'
import { BrowserRouter } from 'react-router-dom'
import { CategoriesProvider } from './context/CategoriesProvider.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProvider>
      <CategoriesProvider>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </CategoriesProvider>
    </AuthProvider>
  </StrictMode>,
)
