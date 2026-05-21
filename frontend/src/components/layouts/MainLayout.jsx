import Header from "./Header";
import Footer from "./Footer";

function MainLayout({ children }) {
  return (
    <>
      <Header />
      <main>{children}</main>
      <Footer />
    </>
  );
}

export default MainLayout;