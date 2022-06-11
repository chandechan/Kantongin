const functions = require('firebase-functions');    // Firebase Functions SDK
const express = require('express');                 // Express web server framework
const cors = require('cors');                       // Enable CORS for all routes
const admin = require('firebase-admin');            // Firebase Admin SDK
const kantonginApp = express();                     // Create a new Express application
const bodyParser = require('body-parser');          // Parse JSON bodies (as sent by API clients)

admin.initializeApp();                              
kantonginApp.use(cors({ origin: true }), bodyParser.json());
const firestoreDB = admin.firestore();

/*  const appAuth = require('./appAuth');
    app.use(appAuth);
*/

kantonginApp.post('/register', async (req, res) => {    // Register a new user
    const { username, email, password } = req.body;
    const targetEmail = firestoreDB.collection('kantongin-user-cred').doc(`${email}`);
    const checkEmail = await targetEmail.get();

    if (!email || !password) {
        res.status(400).json({ success: false, message: 'Please provide an email and password' });
    } else if (checkEmail.exists) {
        res.status(400).json({ success: false, message: 'Email already exists' });
    } else {
        await targetEmail.set({
            username, email, password, createdAt: new Date()
        });
        res.status(201).json({ success: true, message: 'Account created successfully' });
    };
});

kantonginApp.post('/login', async (req, res) => {       // Login a user
    const { email, password } = req.body;
    const targetEmail = firestoreDB.collection('kantongin-user-cred').doc(`${email}`);
    const checkEmail = await targetEmail.get();

    if (!email || !password) {
        res.status(400).json({ success: false, message: 'Please provide an email and password' });
    } else if (!checkEmail.exists) {
        res.status(400).json({ success: false, message: 'Account does not exist' });
    } else {
        const user = await targetEmail.get();
        const userData = user.data();
        if (userData.password === password) {
            res.status(200).json({
                 success: true,
                 message: 'Login successful',
                    user: {
                        username: userData.username,
                        email: userData.email,
                        createdAt: userData.createdAt
                    }
                 });
        } else {
            res.status(400).json({ success: false, message: 'Incorrect password' });
        };
    };
});


kantonginApp.post('/kantongku/:id', async (req, res) => {   // create notes kantongku
    const { note, amount, type, tags, date } = req.body;
    const createdAt = new Date().toISOString();
    const noteId = `${createdAt}-${type}-${tags}`;
    const userId = req.params.id;

    if (note && amount && type && tags && date !== undefined) {
        const noteChar = note.length;
        const noteLimit = 200;

        if (noteChar > noteLimit) {
            res.status(400).send({
                success: false,
                message: 'Note is too long'
            });
        } else if (amount.type !== 'number') {
            res.status(400).send({
                success: false,
                message: 'Amount is not a number'
            });
        } else if (type !== 'IN' || type !== 'EX') {
            res.status(400).send({
                success: false,
                message: 'Type is not valid'
            });
        };

        const newNote = {
            "note": note,
            "amount": amount,
            "type": type,
            "tags": tags,
            "date": date
        };

        await firestoreDB.collection(`kantongku-db/users/${userId}`).doc(`${noteId}`).set(newNote);
        res.status(201).send({
            success: true,
            message: 'Note added successfully'
        });
    };

    res.status(400).send({
        success: false,
        message: 'Please provide all required fields'
    });
});

kantonginApp.get('/kantongku/:id', async (req, res) => {    // read data kantongku
    const userId = req.params.id;
    const userNotes = await firestoreDB.collection(`kantongku-db/users/${userId}`).get();

    let notes = [];
    userNotes.forEach(doc => {
        let id = doc.id;
        let data = doc.data();
        notes.push({
            id, ...data
        });
    });

    res.status(200).send(JSON.stringify(notes));

/*
    if (notes !== undefined) {
        res.status(200).send(JSON.stringify(notes));
    } else {
        res.status(400).send({
            success: false,
            message: 'No notes found'
        });
    };
*/

});

kantonginApp.put('/kantongku/:id', async (req, res) => {    // update data kantongku
    const { noteId, note, amount, type, tags, date } = req.body;
    const userId = req.params.id;

    if (note && amount && type && tags && date !== undefined) {
        const noteChar = note.length;
        const noteLimit = 200;

        if (noteChar > noteLimit) {
            res.status(400).send({
                success: false,
                message: 'Note is too long'
            });
        } else if (amount.type !== 'number') {
            res.status(400).send({
                success: false,
                message: 'Amount is not a number'
            });
        } else if (type !== 'IN' || type !== 'EX') {
            res.status(400).send({
                success: false,
                message: 'Type is not valid'
            });
        };

        const newNote = {
            "note": note,
            "amount": amount,
            "type": type,
            "tags": tags,
            "date": date
        };

        const targetNote = firestoreDB.collection(`kantongku-db/users/${userId}`).doc(`${noteId}`);
        const checkNote = await targetNote.get();

        if (checkNote.exists) {
            await targetNote.update(newNote);
            res.status(200).send({
                success: true,
                message: 'Note updated successfully'
            });
        } else {
            res.status(400).send({
                success: false,
                message: 'Note does not exist'
            });
        };
    };

    res.status(400).send({
        success: false,
        message: 'Please provide all required fields'
    });
});

kantonginApp.delete('/kantongku/:id', async (req, res) => {     // delete data kantongku
    const userId = req.params.id;
    const noteId = req.body.noteId;

    const targetNote = firestoreDB.collection(`kantongku-db/users/${userId}`).doc(`${noteId}`);
    const checkNote = await targetNote.get();

    if (checkNote.exists) {
        await targetNote.delete();

        res.status(200).send({
            success: true,
            message: 'Note deleted successfully'
        });
    } else {
        res.status(400).send({
            success: false,
            message: 'Note does not exist'
        });
    };
});

/* 
-----------------------------------------------------------------------------------------------------------------------------------------
*/

// create data wishlist (ok)
kantonginApp.post('/wishlist/:id', async (req, res) => {
    const { name, quantity, price, date, status } = req.body;
    const createdAt = new Date().toISOString();
    const wishId = `${createdAt}-${price}`;
    const userId = req.params.id;

    if (name && quantity && price && date && status !== undefined) {
        const nameChar = name.length;
        const nameLimit = 200;

        if (nameChar > nameLimit) {
            res.status(400).send({
                success: false,
                message: 'Name is too long'
            });
        } else if (price !== 'number') {
            res.status(400).send({
                success: false,
                message: 'Price is not a number'
            });
        } else if (status !== 'true' || status !== 'false') {
            res.status(400).send({
                success: false,
                message: 'Status is not valid'
            });
        };

        const newWish = {
            "name": name,
            "quantity": quantity,
            "price": price,
            "date": date,
            "status": date
        };

        await firestoreDB.collection(`wishlist-db/users/${userId}`).doc(`${wishId}`).set(newWish);
        res.status(200).send({
            success: true,
            message: 'Wishlist added successfully'
        });
    };

    res.status(400).send({
        success: false,
        message: 'Please provide all required fields'
    });
});

// read data wishlist (ok)
kantonginApp.get('/wishlist/:id', async (req, res) => {
    const userId = req.params.id;
    const userWish = await firestoreDB.collection(`wishlist-db/users/${userId}`).get();

    let wishlist = [];
    userWish.forEach(doc => {
        let id = doc.id;
        let data = doc.data();
        wishlist.push({
            id, ...data
        });
    });

    res.status(200).send(JSON.stringify(wishlist));

    /*
    if (wishlist !== undefined) {
        res.status(200).send(JSON.stringify(wishlist));
    } else {
        res.status(400).send({
            success: false,
            message: 'No wish found'
        });
    };
*/
});

// update data wishlist (ok)
kantonginApp.put('/wishlist/:id', async (req, res) => {
    const { wishId, name, quantity, price, date, status } = req.body;
    const userId = req.params.id;

    if (name && quantity && price && date && status !== undefined) {
        const nameChar = name.length;
        const nameLimit = 200;

        if (nameChar > nameLimit) {
            res.status(400).send({
                success: false,
                message: 'Name is too long'
            });
        } else if (price !== 'number') {
            res.status(400).send({
                success: false,
                message: 'Price is not a number'
            });
        } else if (status !== 'true' || status !== 'false') {
            res.status(400).send({
                success: false,
                message: 'Status is not valid'
            });
        };

        const newWish = {
            "name": name,
            "quantity": quantity,
            "price": price,
            "date": date,
            "status": date
        };

        const targetWish = firestoreDB.collection(`wishlist-db/users/${userId}`).doc(`${wishId}`);
        const checkWish = await targetWish.get();

        if (checkWish.exists) {
            await targetWish.update(newWish);
            res.status(200).send({
                success: true,
                message: 'Wishlist updated successfully'
            });
        } else {
            res.status(400).send({
                success: false,
                message: 'Wishlist does not exist'
            });
        };
    };

    res.status(400).send({
        success: false,
        message: 'Please provide all required fields'
    });
});

// delete data wishlist (ok)
kantonginApp.delete('/wishlist/:id', async (req, res) => {
    const userId = req.params.id;
    const wishId = req.body.wishId;

    const targetWish = firestoreDB.collection(`wishlist-db/users/${userId}`).doc(`${wishId}`);
    const checkWish = await targetWish.get();

    if (checkWish.exists) {
        await targetWish.delete();

        res.status(200).send({
            success: true,
            message: 'Wishlist deleted successfully'
        });
    } else {
        res.status(400).send({
            success: false,
            message: 'Wishlist does not exist'
        });
    };
});

/* 
-----------------------------------------------------------------------------------------------------------------------------------------
*/

// create data payment (ok)
kantonginApp.post('/payment/:id', async (req, res) => {
    const { name, quantity, price, date, status } = req.body;
    const createdAt = new Date().toISOString();
    const paymentId = `${createdAt}-${price}`;
    const userId = req.params.id;

    if (name && quantity && price && date && status !== undefined) {
        const nameChar = name.length;
        const nameLimit = 200;

        if (nameChar > nameLimit) {
            res.status(400).send({
                success: false,
                message: 'Name is too long'
            });
        } else if (price !== 'number') {
            res.status(400).send({
                success: false,
                message: 'Price is not a number'
            });
        } else if (status !== 'true' || status !== 'false') {
            res.status(400).send({
                success: false,
                message: 'Status is not valid'
            });
        };

        const newPayment = {
            "name": name,
            "quantity": quantity,
            "price": price,
            "date": date,
            "status": date
        };

        await firestoreDB.collection(`payment-db/users/${userId}`).doc(`${paymentId}`).set(newPayment);
        res.status(200).send({
            success: true,
            message: 'Payment added successfully'
        });
    };

    res.status(400).send({
        success: false,
        message: 'Please provide all required fields'
    });
});

// read data payment (ok)
kantonginApp.get('/payment/:id', async (req, res) => {
    const userId = req.params.id;
    const userPayment = await firestoreDB.collection(`payment-db/users/${userId}`).get();

    let payment = [];
    userPay.forEach(doc => {
        let id = doc.id;
        let data = doc.data();
        payment.push({
            id, ...data
        });
    });

    res.status(200).send(JSON.stringify(Payment));

    /*
    if (payment !== undefined) {
        res.status(200).send(JSON.stringify(payment));
    } else {
        res.status(400).send({
            success: false,
            message: 'No payment found'
        });
    };
*/
});

// update data payment (ok)
kantonginApp.put('/payment/:id', async (req, res) => {
    const { paymentId, name, quantity, price, date, status } = req.body;
    const userId = req.params.id;

    if (name && quantity && price && date && status !== undefined) { 
        const nameChar = name.length;
        const nameLimit = 200;

        if (nameChar > nameLimit) {
            res.status(400).send({
                success: false,
                message: 'Name is too long'
            });
        } else if (price !== 'number') {
            res.status(400).send({
                success: false,
                message: 'Price is not a number'
            });
        } else if (status !== 'true' || status !== 'false') {
            res.status(400).send({
                success: false,
                message: 'Status is not valid'
            });
        };

        const newPayment = {
            "name": name,
            "quantity": quantity,
            "price": price,
            "date": date,
            "status": date
        };

        const targetPayment = firestoreDB.collection(`payment-db/users/${userId}`).doc(`${paymentId}`);
        const checkPayment = await targetPayment.get();

        if (checkPayment.exists) {
            await targetPayment.update(newPayment);
            res.status(200).send({
                success: true,
                message: 'Payment updated successfully'
            });
        } else {
            res.status(400).send({
                success: false,
                message: 'Payment does not exist'
            });
        };
    };

    res.status(400).send({
        success: false,
        message: 'Please provide all required fields'
    });
});

// delete data payment (ok)
kantonginApp.delete('/payment/:id', async (req, res) => {
    const userId = req.params.id;
    const paymentId = req.body.paymentId;

    const targetPayment = firestoreDB.collection(`payment-db/users/${userId}`).doc(`${paymentId}`);
    const checkPayment = await targetPayment.get();

    if (checkPayment.exists) {
        await targetPayment.delete();

        res.status(200).send({
            success: true,
            message: 'Payment deleted successfully'
        });
    } else {
        res.status(400).send({
            success: false,
            message: 'Payment does not exist'
        });
    };
});

exports.kantonginAppBeta = functions.https.onRequest(kantonginApp);


