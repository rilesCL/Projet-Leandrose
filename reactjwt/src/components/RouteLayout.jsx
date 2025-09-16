import { Outlet } from 'react-router';

function RouteLayout() {
    return (
        <>
            <main>
                <Outlet />
            </main>
        </>
    );
}

export default RouteLayout;