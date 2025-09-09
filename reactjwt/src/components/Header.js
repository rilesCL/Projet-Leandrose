import React from 'react';
import './Header.css';
import { Link } from 'react-router-dom';

function Header({user}) {
  return (
    <header className="header">
      <h1>My App</h1>
      <nav>
        <ul className="nav-links">
          <li><Link to="/">Accueil</Link></li>
          <li><Link to="/about">À propos</Link></li>
          <li><Link to="/emprunteur">Emprunteur</Link></li>
          <li><Link to="/prepose">Prepose</Link></li>
          <li><Link to="/gestionnaire">Gestionnaire</Link></li>
          <li>{
            user ? <Link to="/logout">Logout</Link> : <Link to="/login">Login</Link>
          }</li>
        </ul>
        {user &&
          <p className="para-align">Bonjour {user.firstName} {user.lastName} {user.role}</p>
        }
      </nav>
    </header>
  );
}

export default Header;