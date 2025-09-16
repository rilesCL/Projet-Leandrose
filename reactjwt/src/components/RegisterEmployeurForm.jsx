import React, { useState } from "react";
import {registerEmployeur} from "../api/apiEmployeur.jsx";
import {useNavigate} from "react-router";

function RegisterEmployeur() {
    const navigate = useNavigate();
    const [employeur, setEmployeur] = useState({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        companyName: "",
        field: ""
    });
    const [error, setError] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (e) => {
        const { id, value } = e.target;
        setEmployeur({
            ...employeur,
            [id]: value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError("");

        try {
            await registerEmployeur(employeur);
            navigate("/");
        } catch (error) {
            if (error.response && error.response.data) {
                setError(error.response.data);
            } else {
                setError("Une erreur est survenue.");
            }
        }
        finally {
            setIsSubmitting(false);
        }
    };

    const handleReset = () => {
        setEmployeur({
            firstName: "",
            lastName: "",
            email: "",
            password: "",
            companyName: "",
            field: ""
        });
        setError("");
    };

    return (
        <div>
            ASFGUDIHGORSAGCVHFJKASBHF
        </div>
    );
}

export default RegisterEmployeur;