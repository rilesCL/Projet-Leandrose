import {Outlet} from 'react-router-dom';

export default function RouteLayout() {
    return (
        <div className="w-full min-h-screen">
            <main className="w-full">
                <Outlet/>
            </main>
        </div>
    );
}