CREATE TABLE User_Table (
    user_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR2(100) NOT NULL,
    password VARCHAR2(50) NOT NULL,
    email VARCHAR2(100) UNIQUE NOT NULL,
    phone_number VARCHAR2(20),
    aadhar_number VARCHAR2(20) UNIQUE NOT NULL,
    user_type VARCHAR2(10) CHECK (user_type IN ('owner', 'renter'))
);

CREATE TABLE Owner_Table (
    owner_id NUMBER PRIMARY KEY REFERENCES User_Table(user_id),
    address VARCHAR2(255) NOT NULL
);


CREATE TABLE Pg_Table (
    pg_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    owner_id NUMBER REFERENCES Owner_Table(owner_id),
    pg_name VARCHAR2(100) NOT NULL,
    area VARCHAR2(100) NOT NULL,
    available_rooms NUMBER NOT NULL,
    rent NUMBER NOT NULL
);

CREATE TABLE Booking_Table (
    booking_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pg_id NUMBER REFERENCES Pg_Table(pg_id),
    renter_id NUMBER REFERENCES User_Table(user_id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    booking_date DATE DEFAULT CURRENT_DATE,
    token_amount NUMBER NOT NULL CHECK (token_amount >= 3000),
    booking_status VARCHAR2(10) CHECK (booking_status IN ('confirmed', 'pending'))
);
