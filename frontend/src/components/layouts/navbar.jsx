import "./navbar.css";

function Navbar() {
  return <h1>Thanh navbar default</h1>;
}

function NavbarHomePage() {
  return (
    <nav className="navbar-homepage">
      {/* Logo */}
      <a href="#heroSection" className="logo">
        <div className="logo-img">
          <img src=".\public\logo.png" alt="AI Study Hub logo" />
        </div>
        <div className="logo-Name">
          <a href="#heroSection">AI Study Hub</a>
        </div>
      </a>

      {/* Nav links */}
      <div className="menu">
        <ul>
          <li>
            <a href="#heroSection">Hero</a>
          </li>
          <li>
            <a href="#featureSection">Features</a>
          </li>
          <li>
            <a href="#popularDocumentSection">Documents</a>
          </li>
          <li>
            <a href="#ai-showcase">AI Showcase</a>
          </li>
          <li>
            <a href="#statistics">Statistics</a>
          </li>
        </ul>
      </div>

      {/* Auth buttons */}
      <div className="auth">
        <ul>
          <li>
            <a href="/Login">Login</a>
          </li>
          <li>
            <a href="/Signup">Sign Up</a>
          </li>
        </ul>
      </div>
    </nav>
  );
}

export default Navbar;
export { NavbarHomePage };
