import AiShowcaseSection from "./component/AiShowcaseSection";
import FeatureSection from "./component/FeatureSection";
import PopularDocumentSection from "./component/PopularDocumentSection";
import StatisticSection from "./component/StatisticSection";
import HeroSection from "./component/HeroSection";
import Navbar, { NavbarHomePage } from "../../components/layouts/navbar";
import Header from "../../components/layouts/Header";
import Footer from "../../components/layouts/Footer";
function HomePage() {
  return (
    <div className="container">
      <NavbarHomePage />
      <HeroSection />
      <FeatureSection />
      <PopularDocumentSection />
      <AiShowcaseSection />
      <StatisticSection />
    </div>
  );
}

export default HomePage;
