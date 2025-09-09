import {Outlet} from "react-router-dom";
import Header from "./Header";
import Footer from "./Footer";
import './PageLayout.css'

function PageLayout ({user}) {
  return (
    <div id="pagelayout" className="pageLayout">
      <Header user={user}/>
      <Outlet />
      <Footer />
    </div>
  );

}
export default PageLayout;