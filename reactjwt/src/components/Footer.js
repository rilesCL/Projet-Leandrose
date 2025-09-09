import {Link } from 'react-router-dom'
import './Footer.css'

const Footer = () => {
    return (
        <footer className="footercss">
            <p>Copyright &copy; 2021</p>
            <Link to='/about'>About</Link>
        </footer>
    )
}

export default Footer
