import { Outlet } from 'react-router-dom';

export default function RouteLayout() {
    return (
        <div className="w-full min-h-screen">
            <main className="w-full px-4 sm:px-6 lg:px-8 py-6">
                <Outlet />
            </main>
        </div>
    );
}